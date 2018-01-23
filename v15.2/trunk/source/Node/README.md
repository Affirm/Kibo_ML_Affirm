# Marketlive Node Scripts
Built with node v0.10.33, npm v1.4.28

- - - -

## Getting Started

### Install Node
Download the appropriate version of [node.js](https://nodejs.org/download/) for your system

### Install Grunt
    npm install -g grunt-cli
    
If you get errors while trying ro run the command above, you may need to run the command as a 'sudo' user:

    sudo npm install -g grunt-cli)    

### Install the Dependencies
    npm install
Make sure you are in the site's source node directory '/sites/SiteName/trunk/source/Node'

### 

- - - -

## Tasks
You can get a list of all avaible tasks by running:

    grunt tasks

Which should output:    

    watch                  >  Watches for changes and compiles/deploys as necessary.     
    buildLess             =>  Builds Less (compiles/deploys) by groupName or all.     
    buildNewerLess        =>  Same as buildLess, but only deploys newer/changed Less files.     
    compileAllLessGroups  =>  Compiles all less groups into their corresponding css files.     
    compileLessGroup      =>  Compiles a given less group into its corresponding css file.     
    Done, without errors.
The main tasks are the **watch** and **buildLess** tasks

### Task - watch
This task will automatically compile and deploy the groups/files as you work, for a given site's overrides folder. The watch command tries to be efficient about what it’s compiling and when. For example, if it
notices a change on a group’s less file, it will only compile and deploy the files for that
group. However, if it notices changes made to ‘global’ files, it will compile and deploy all groups. 

(* Change the the site "ml5" to match your local site and "ml5-src-app" to match as well)    

For Product Dev **Source** builds:

    grunt watch --source $MARKETLIVE_HOME/dev/trunk/source/Apps/B2CWeb --deploy $MARKETLIVE_HOME/sites/ml5/trunk/deploy/ml5-src-app --overrides default_overrides

For CS Dev **Release** builds:

    grunt watch --source $MARKETLIVE_HOME/sites/ml5/trunk/source/Apps/B2C --deploy $MARKETLIVE_HOME/sites/ml5/trunk/deploy/ml5-rel-app --overrides default_overrides

(* Note: the only difference between source and release commands are the directories being passed
in.)

### Task - buildLess
This task will compile and deploy a specific group (e.g. buildLess:account) or all (e.g. buildLess:all) groups manually, for a given site's overrides folder. The buildLess command would be run manually, in place of the background watch command. This task also deploys files locally, upon completion, and thus will not require a restart in order to see the changes take effect on the locally running site. 

Build all less: (compiles all groups in parallel)

    grunt buildLess:all --source $MARKETLIVE_HOME/dev/trunk/source/Apps/B2CWeb --deploy $MARKETLIVE_HOME/sites/ml5/trunk/deploy/ml5-src-app --overrides default_overrides

Build less group: (compiles the specified group, which is checkout in this case)

    grunt buildLess:checkout --source $MARKETLIVE_HOME/dev/trunk/source/Apps/B2CWeb --deploy $MARKETLIVE_HOME/sites/ml5/trunk/deploy/ml5-src-app --overrides default_overrides

### Options
    --source (required)             The local source directory
    --deploy (required)             The local deploy directory
    --overrides (required)          The name of the site’s overrides folder
    --skipSourceDeploy (optional)   Skip source deploy on watch, for cases where the developers IDE is already doing this 
