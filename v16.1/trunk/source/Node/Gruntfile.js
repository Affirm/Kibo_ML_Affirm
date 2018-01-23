/*jslint node: true */

module.exports = function (grunt) {
    'use strict';

    var marketlive = {},
        marketliveWatch = {},
        MlGroup,
        groupJSON;

    // Load grunt tasks automatically.
    require('load-grunt-tasks')(grunt);

    // Time how long tasks take.
    //require('time-grunt')(grunt);

    // configurable paths
    // for cli pass options: --source /path  --deploy /path --overrides siteOverridesDirectoryName --skipSourceDeploy
    marketlive = {
        paths: {
            source: grunt.option('source') || '/java/marketlive/dev/trunk/source/Apps/B2CWeb',
            deploy: grunt.option('deploy') || '/java/marketlive/sites/ml5/trunk/deploy/ml5-src-app',
            overrides: grunt.option('overrides') || 'default_overrides',
            less: '/wwwroot/WEB-INF/less',
            compiled: '/wwwroot/WEB-INF/css/compiled',
            javaScript: '/wwwroot/includes'
        },
        compileGroups: []
    };

    /**
     * Constructor for MLGroup objects.
     * @class
     * @param {{[configAsIs]: boolean, groupName: string, [lessDir]: string,
     *  [lessFile]: string, [cssFile]: string}} params
     */
    MlGroup = function MlGroup(params) {
        this.configAsIs = params.configAsIs;
        this.groupName = params.groupName;
        this.lessDir = params.lessDir;
        this.lessFile = params.lessFile;
        this.cssFile = params.cssFile;

        // Only continue to 'flesh out' the configs if we're not taking the config 'as is'
        if (!this.configAsIs) {
            // Build the less directory path for the group
            if (this.lessDir) {
                this.lessDir = marketlive.paths.deploy + marketlive.paths.less + this.lessDir;
            } else if (this.groupName) {
                this.lessDir = marketlive.paths.deploy + marketlive.paths.less + '/marketlive/' + this.groupName + '/';
            }

            // Build the less file path for the group
            if (!this.lessFile) {
                this.lessFile = this.lessDir + 'standalone.less';
            } else {
                this.lessFile = this.lessDir + this.lessFile;
            }

            // Build the css file path for the group
            if (this.groupName && !this.cssFile) {
                this.cssFile = marketlive.paths.source + marketlive.paths.compiled + '/' +
                    marketlive.paths.overrides + '_' + this.groupName + '.css';
            } else if (this.cssFile) {
                this.cssFile = marketlive.paths.source + marketlive.paths.compiled + '/' +
                    marketlive.paths.overrides + '_' + this.cssFile + '.css';
            }
        }
    };

    // Set up the LessCss groups
    marketlive.groups = {};
    groupJSON = grunt.file.readJSON('groups.json');
    for (var i = 0; i < groupJSON.length; i++) {
        marketlive.groups[groupJSON[i].groupName] = new MlGroup(groupJSON[i]);
    }

    // Set up watchers for all LessCss groups, and build out the 'compileGroups' array for concurrent compilation.
    for (var group in marketlive.groups) {
        if (marketlive.groups.hasOwnProperty(group)) {
            var compileTask = 'exec:compileLess:' + marketlive.groups[group].lessFile + ':' +
                marketlive.groups[group].cssFile;

            // Set up a watcher for this group
            marketliveWatch[group + '_less'] = {
                files: [marketlive.groups[group].lessDir + '{,**/}*.less'],
                tasks: [compileTask]
            };

            // Add the group's compileTask to the compileGroups array  for concurrent compilation.
            marketlive.compileGroups.push(compileTask);
        }
    }

    // Add one-off watcher for source Less files, so we can copy them to the deploy directory
    if (!grunt.option('skipSourceDeploy')) {
        marketliveWatch.less = {
            files: [marketlive.paths.source + marketlive.paths.less + '/{,**/}*.less'],
            tasks: ['newer:copy:deployLessOnly']
        };
    }

    // Add one-off watcher for CSS files, so we can copy them to the deploy directory
    marketliveWatch.css = {
        files: [marketlive.paths.source + marketlive.paths.compiled + '/{,**/}*.css'],
        tasks: ['newer:copy:deployCSSOnly']
    };

    // Add one-off watcher for the sites/embed, global and common folders, so we can call the
    // buildNewerLess:all task on changes
    marketliveWatch.embed = {
        files: [
            marketlive.paths.deploy + marketlive.paths.less + '/sites/' +
            marketlive.paths.overrides + '/embed{,**/}*.less',
            marketlive.paths.deploy + marketlive.paths.less + '/marketlive/global{,**/}*.less',
            marketlive.paths.deploy + marketlive.paths.less + '/marketlive/common{,**/}*.less',
            marketlive.paths.deploy + marketlive.paths.less + '/marketlive/thirdparty{,**/}*.less'
        ],
        tasks: ['buildNewerLess:all']
    };

    // Add one-off watcher for JavaScript
    if (!grunt.option('skipSourceDeploy')) {
        marketliveWatch.js = {
            files: [marketlive.paths.source + marketlive.paths.javaScript + '/{,**/}*.js'],
            tasks: ['newer:copy:deployAll']
        };
    }

    // Define configuration for tasks.
    grunt.initConfig({
        // Project settings
        marketlive: marketlive,

        // Watch files
        watch: marketliveWatch,

        // Configure concurrent tasks
        concurrent: {
            allLess: {
                tasks: '<%= marketlive.compileGroups %>'
            }
        },

        // Configure available tasks list
        availabletasks: {
            tasks: {
                options: {
                    filter: 'include',
                    tasks: ['watch', 'buildLess', 'buildNewerLess', 'compileAllLessGroups', 'compileLessGroup'],
                    sort: ['watch', 'buildLess', 'buildNewerLess', 'compileAllLessGroups', 'compileLessGroup'],
                    descriptions: {
                        'watch': 'Watches for changes and compiles/deploys as necessary.'
                    }
                }
            }
        },

        // Configure less compilation
        exec: {
            compileLess: {
                cmd: function (input, output) {
                    return 'node node_modules/less/bin/lessc -x --global-var="ml-site-overrides-folder=' +
                        marketlive.paths.overrides + '" ' + input + ' ' + output;
                }
            }
        },

        // Configure copy/deploy tasks
        copy: {
            deployAll: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= marketlive.paths.source %><%= marketlive.paths.less %>',
                        src: '{,**/}*.less',
                        dest: '<%= marketlive.paths.deploy %><%= marketlive.paths.less %>'
                    },
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= marketlive.paths.source %><%= marketlive.paths.compiled %>',
                        src: '{,**/}*.css',
                        dest: '<%= marketlive.paths.deploy %><%= marketlive.paths.compiled %>'
                    },
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= marketlive.paths.source %><%= marketlive.paths.javaScript %>/',
                        src: '{,**/}*.js',
                        dest: '<%= marketlive.paths.deploy %><%= marketlive.paths.javaScript %>'
                    }
                ]
            },
            deployLessOnly: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= marketlive.paths.source %><%= marketlive.paths.less %>',
                        src: '{,**/}*.less',
                        dest: '<%= marketlive.paths.deploy %><%= marketlive.paths.less %>'
                    }
                ]
            },
            deployCSSOnly: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= marketlive.paths.source %><%= marketlive.paths.compiled %>',
                        src: '{,**/}*.css',
                        dest: '<%= marketlive.paths.deploy %><%= marketlive.paths.compiled %>'
                    }
                ]
            }
        }
    });

    // Builds Less (compiles/deploys) by groupName or all.
    grunt.registerTask('buildLess', 'Builds Less (compiles/deploys) by groupName or all.',
        function (group) {
            grunt.task.run(['copy:deployLessOnly']);

            if (group === 'all') {
                grunt.task.run(['compileAllLessGroups']);
            } else if (group) {
                grunt.task.run(['compileLessGroup:' + group]);
            }

            grunt.task.run(['newer:copy:deployCSSOnly']);
        }
    );

    // Same as buildLess, but only deploys newer/changed Less files.
    grunt.registerTask('buildNewerLess', 'Same as buildLess, but only deploys newer/changed Less files.',
        function (group) {
            grunt.task.run(['newer:copy:deployLessOnly']);

            if (group === 'all') {
                grunt.task.run(['compileAllLessGroups']);
            } else if (group) {
                grunt.task.run(['compileLessGroup:' + group]);
            }

            grunt.task.run(['newer:copy:deployCSSOnly']);
        }
    );

    // Compiles all less groups into their corresponding css files.
    grunt.registerTask('compileAllLessGroups', 'Compiles all less groups into their corresponding css files.',
        function () {
            grunt.task.run(['concurrent:allLess']);
        }
    );

    // Compiles a given less group into its corresponding css file.
    grunt.registerTask('compileLessGroup', 'Compiles a given less group into its corresponding css file.',
        function (group) {
            grunt.task.run(['exec:compileLess:' + marketlive.groups[group].lessFile + ':' +
                marketlive.groups[group].cssFile]);
        }
    );

    // Alias 'availabletasks' with 'tasks'
    grunt.registerTask('tasks', ['availabletasks']);

    // Register the watch task
    grunt.registerTask('default', [
        'watch'
    ]);
};
