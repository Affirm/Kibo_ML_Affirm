/*! controllers.js | (c) 2014 MarketLive, Inc. | All Rights Reserved */

//This option lets you control how nested do you want your blocks to be
/*jshint maxdepth: 6 */

window.angular = window.angular || {};

(function (angular) {
    'use strict';

    /*******************************************************************************************************************
     * Type definitions
     ******************************************************************************************************************/

    /**
     * The Step object contains display state, validation rules, and form mapping data.
     * @typedef {object} Step
     * @property {string} heading - The step heading.
     * @property {boolean} collapsed - Indicates whether the step is collapsed.
     * @property {boolean} accessible - Indicates whether the step is accessible.
     * @property {boolean} userCollapsible - Indicates whether the step is user collapsible.
     * @property [{
     *      header: string,
     *      values: string[]}]} summary - An array of summary objects.
     * @property {{
     *      rules: object,
     *      messages: object}} validation - Contains the validation rules and messages for the step.
     */

    /**
     * The Shipping object contains the options for shipTo, address and delivery along with their selected state.
     * @typedef {object} Shipping
     * @property {{
     *      options: [{label: string, value: string}],
     *      selectedOption: string}} shipTo - Contains the 'ship to' options.
     * @property {{
     *      options: [{label: string, value: string}],
     *      selectedOption: string}} addresses - Contains the address options.
     * @property {{
     *      options: [{label: string, value: string}],
     *      selectedOption: string}} delivery - Contains the delivery options..
     * @property {object} gifting - Contains the gifting options..
     */

    /**
     * The Payment object contains data relevant to payments, such as available credit card types, etc.
     * @typedef {object} Payment
     */

    /**
     * The Summary object contains a summary of the sub totals and order totals.
     * @typedef {object} Summary
     * @property {[{
     *      label: string,
     *      value: string,
     *      special: boolean}]} subTotals - An array of subTotal objects.
     * @property {[{
     *      label: string,
     *      value: string,
     *      special: boolean}]} orderTotals - An array of orderTotals objects.
     */

    /**
     * An object used to update model data
     * @typedef {object} TargetDataItem
     * @property {string} path - The path to the model data (example: 'shipping.shipments').
     * @property {{key: string, value: *}} findBy - A key/value object used to find Objects within Arrays.
     * @property {boolean} extend - Used to determine if the object should be extended or overridden entirely.
     * @property {object} data - The data to update the model with.
     */

    /*******************************************************************************************************************
     * End Type definitions
     ******************************************************************************************************************/

        // Create the angular controller
    angular.module('mlAccordionCheckoutApp').controller('mlAccordionCheckoutCtrl',
        ['$scope', '$log', 'mlAccordionCheckoutService', function($scope, $log, mlAccordionCheckoutService){

            $scope.modelInitialized = false;
            $scope.clientSideValidationMsg = '';

            // Model Data
            $scope.model = {
                user: '',
                /** @type {Step[]} */
                steps: [],
                /** @type {Shipping} */
                shipping: {},
                /** @type {Payment} */
                payment: {},
                /** @type {Summary} */
                summary: {},
                basketItems: [],
                stateData: {
                    processingRequest: false,
                    deliveryStepIndex: -1,
                    activeShipment: {},
                    xsBasketToggle: false
                },
                socialProfile: {
                    firstName: '',
                    lastName: '',
                    email: '',
                    phoneNumber: '',
                    city: '',
                    streetAddress: ''
                }
            };

            /**
             * Set the initial state for the model data.
             */
            $scope.init = function () {
                // Log out
                $log.debug('-- init -- ');

                // Make the call to get the init data
                $scope.getData({params:{method:'initData'}, callback:function(data){
                    // Mark the model as being initialized
                    $scope.modelInitialized = true;

                    // Find and store the step index of the delivery step
                    $scope.model.stateData.deliveryStepIndex = $scope.getStepIndexById('delivery');

                    // fire reporting event
                    if (data !== undefined && data.reporting !== undefined && data.reporting.data !== undefined) {
                        var reportingData = data.reporting.data;
                        $log.debug(reportingData);
                        MarketLive.Events.accordionCheckoutInitialized.trigger({reportingData: reportingData});
                    }
                    /**
                     * saving pre-fill socialProfile in the new address form to be using while consumer click on creating new address
                     */
                    $scope.model.socialProfile.firstName = $('#addressBookForm #firstName').val();
                    $scope.model.socialProfile.lastName = $('#addressBookForm #lastName').val();
                    $scope.model.socialProfile.email = $('#addressBookForm #emailAddress').val();
                    $scope.model.socialProfile.phoneNumber = $('#addressBookForm #dayPhone').val();
                    $scope.model.socialProfile.city = $('#addressBookForm #city').val();
                    $scope.model.socialProfile.streetAddress = $('#addressBookForm #streetAddress').val();

                    // Log out
                    $log.debug('init: the data has been initialized');
                }});
            };

            /**
             * Submit the 'step' at the provided 'stepIndex'.
             * @param {Object} event
             * @param {number} stepIndex
             */
            $scope.submitStep = function(event, stepIndex) {
                // Log out
                $log.debug('-- submitStep --');

                // Stop event propagation & prevent the default behavior
                event.stopPropagation();
                event.preventDefault();
                var stepId = $scope.model.steps[stepIndex].id; 
                // Post the step if validation passes, otherwise keep the user on this step
                if ($scope.validateForm({'stepIndex': stepIndex})) {
                    $log.debug('submitStep: validation passed');
                    $scope.logStepSuccess({'stepIndex': stepIndex});
                   // apply GC before submit the payment step 
                    if(stepId === 'pay' && $scope.model.payment.giftCertificateNumber !== undefined && $scope.model.payment.giftCertificateNumber !== '') {
                    	$scope.postData({'method':'applyGiftCertificate','data':'gcNumber='+$scope.model.payment.giftCertificateNumber})
                    }
                    
                    //Affirm payment method
                    var affirmMethodUsed = false;
                    var paymentTypes = $('input[name="paypalCheckoutSelected"]:checked');
                    if (stepId === 'order' && paymentTypes !== null && paymentTypes !== 'undefined' && paymentTypes.length > 0 && paymentTypes.is(':checked')) {
                    	affirmMethodUsed = 'AFFIRM_YES' === paymentTypes.val(); 
                    }
                    if(stepId === 'order' && affirmMethodUsed) {
                    	$scope.postData({'stepIndex':stepIndex, method:'submitStep',callback:function(data){
                    		if(data.config != undefined) {
                    			console.log('affirm json is:', JSON.stringify(data));
                    			
                    			affirm.checkout(data);
                    			affirm.ui.ready(
                    					function() {
                    						affirm.ui.error.on("close", function(){
                    							$scope.postData({'stepIndex':stepIndex});
                    					});
                    				}
                    			);
                    			affirm.checkout.post();
		                    } else {
		                    	$scope.updateModel(data);
		                    }
		                   }
		                })
                    } else { 
                    	$scope.postData({'stepIndex':stepIndex, method:'submitStep'});
                    }
                } else {
                    // Log out
                    $log.debug('submitStep: validation failed');
                    $scope.logStepError({'stepIndex': stepIndex, 'message': $scope.clientSideValidationMsg});
                }
            };

            /**
             * Validate the form for the 'step' at the provided 'stepIndex', or the form at the provided 'targetForm'
             * @param {{stepIndex: number, [targetForm]: object}} paramObj
             * @returns {boolean}
             */
            $scope.validateForm = function (paramObj) {
                var stepIndex = paramObj.stepIndex,
                    validationOptions,
                    targetForm,
                    validator,
                    isValid = true;

                /** @type {Step} */
                var step = $scope.model.steps[stepIndex];
                var stepId = $scope.model.steps[stepIndex].id;   
                // read the property app.b2c.checkout.PaymentModel.cc_required to check if CC validation required from client side.
                var isCCRequired = $scope.model.payment.ccrequired;                

                // Log out
                $log.debug('-- validateForm --');

                // Set the 'targetForm'
                if (paramObj.targetForm) {
                    targetForm = paramObj.targetForm;
                } else {
                    targetForm = angular.element('.ml-accordion-steps > li:eq('+ stepIndex +')').find('form:not([target]):eq(0)');
                }

                // Only continue if we have 'validation' for the current step
                // In case of payment step, validate only if cc_required is set to true
                if (step.validation && (stepId !== 'pay' || isCCRequired)){
                    // Setup the validation options
                    validationOptions = angular.element.extend({},
                    MarketLive.ClientSideValidate.defaults, step.validation);

                    // Setup the validator
                    validator = targetForm.validate(validationOptions);

                    // Validate the form
                    validator.form();

                    // Update the 'isValid' boolean
                    if (validator.numberOfInvalids() === 0) {
                        isValid = true;
                    } else {
                        isValid = false;

                        // Focus on the first failed field
                        targetForm.find('.ml-csvalidation-error').filter('input, select, textarea').
                            filter(':first').focus().trigger('focusin');

                        $scope.clientSideValidationMsg = validator.errorList && validator.errorList.length > 0 &&
                            validator.errorList[0].message ? validator.errorList[0].message : '';
                    }
                }

                return isValid;
            };

            /**
             * Reset the form validation  for the 'step' at the provided 'stepIndex',
             * or the form at the provided 'targetForm'.
             * @param {{stepIndex: number, [targetForm]: object}} paramObj
             * @returns {boolean}
             */
            $scope.resetValidation = function (paramObj) {
                var stepIndex = paramObj.stepIndex,
                    validationOptions,
                    targetForm,
                    validator;

                // Log out
                $log.debug('-- resetValidation --');

                /** @type {Step} */
                var step = $scope.model.steps[stepIndex];

                // Set the 'targetForm'
                if (paramObj.targetForm) {
                    targetForm = paramObj.targetForm;
                } else {
                    targetForm = angular.element('.ml-accordion-steps > li:eq('+ stepIndex +')').find('form:not([target]):eq(0)');
                }

                // Clear field values
                targetForm.find('input, select').each(function () {
                    var element = angular.element(this),
                        tagName = element[0].tagName,
                        type = element.attr('type');

                    if (tagName === 'INPUT') {
                        switch (type) {
                            case 'text':
                            case 'tel':
                            case 'email':
                            case 'phone':
                                element.val('');
                                break;
                            case 'checkbox':
                                element.prop('checked', false);
                                break;
                        }
                    } else if (tagName === 'SELECT') {
                        element.val('');
                    }
                });

                // Remove the CSS error class
                targetForm.find('.ml-csvalidation-error').filter('input, select').removeClass('ml-csvalidation-error');

                // Only continue if we have 'validation' for the current step
                if (step.validation) {
                    // Setup the validation options
                    validationOptions = angular.element.extend({},
                        MarketLive.ClientSideValidate.defaults, step.validation);

                    // Setup the validator
                    validator = targetForm.validate(validationOptions);

                    // Reset the form
                    validator.resetForm();
                }
            };

            /**
             * Get data from the service, update the model data on return, and execute an optional callback.
             * @param {{params: object, [callback]: function}} paramObj
             */
            $scope.getData = function (paramObj) {
                // Log out
                $log.debug('-- getData --');
                $log.debug('getData: params: ' + JSON.stringify(paramObj.params));

                $scope.model.stateData.processingRequest = true;

                // Call the service to 'GET' data.
                mlAccordionCheckoutService.getData(paramObj.params).then(function (data) {
                    // Log out
                    $log.debug('getData: data returned');

                    // Check for redirects
                    if (data.redirect) {
                        window.location.href = data.redirect;
                        return;
                    }

                    // Update the model with data
                    $scope.updateModel(data);

                    // Execute the callback, if we have one
                    if (typeof(paramObj.callback) === 'function') {
                        paramObj.callback(data);
                    }
                    $scope.model.stateData.processingRequest = false;
                }, function () {
                    // Log out
                    $log.debug('getData: failed to receive data');
                    $scope.model.stateData.processingRequest = false;
                });
            };

            /**
             * Post data to the service and update the model data on return.
             * @param {{[stepIndex]: number,
          *         [targetForm]: object,
          *         [method]: string,
          *         [data]: string,
          *         [callback]: function}} paramObj
             */
            $scope.postData = function (paramObj) {
                var stepIndex = paramObj.stepIndex,
                    postMethod = paramObj.method || 'updateStepData',
                    postData = '',
                    targetForm;

                // Bail out if the model hasn't been initialized yet,
                // or if we're trying to post data for a step that is not accessible
                // or if we're already processing a request
                if (!$scope.modelInitialized || (stepIndex !== undefined &&
                    $scope.model.steps.length > 0 && !$scope.model.steps[stepIndex].accessible) ||
                    $scope.model.stateData.processingRequest === true) {
                    return;
                }

                // Log out
                $log.debug('-- postData --');

                // Set the 'targetForm'
                if (paramObj.targetForm) {
                    targetForm = paramObj.targetForm;
                } else if (stepIndex !== undefined && paramObj.data === undefined) {
                    targetForm = angular.element('.ml-accordion-steps > li:eq('+ stepIndex +')').find('form:not([target]):eq(0)');
                }

                // Set the 'postData'
                if (paramObj.data){
                    postData = paramObj.data;
                } else if (targetForm !== undefined) {
                    postData = targetForm.serialize();
                }

            // Only execute this block if we have a 'stepIndex'
            if (stepIndex !== undefined) {
                // Add the 'stepIndex' and stepId to the outgoing 'postData'
                postData += (postData.length > 0)? ('&' + 'stepIndex=' + stepIndex) : ('stepIndex=' + stepIndex);
                postData += '&stepId=' + $scope.model.steps[stepIndex].id;
                if(paramObj.clearBfDutyData) {
                   postData += '&clearBfDutyData=' + paramObj.clearBfDutyData;
                }
            }

                // Log out
                $log.debug('postData: stepIndex: ' + stepIndex);
                $log.debug('postData: targetForm: ' + targetForm);
                $log.debug('postData: postMethod: ' + postMethod);
                $log.debug('postData: postData: ' + postData);

                $scope.model.stateData.processingRequest = true;

                // Post the data to the backend for processing
                mlAccordionCheckoutService.postData(postMethod, postData).then(function (data) {
                    // Log out
                    $log.debug('postData:  data returned');

                    // measure Google Analytics Enhanced Ecommerce checkout options
                    if ((data.steps || data.redirect) && postMethod !== 'saveAndContinueAddress') {
                        var googleAnalytics = null;
                        if (data && data.reporting && data.reporting.data && data.reporting.data.googleanalytics) {
                            googleAnalytics = data.reporting.data.googleanalytics;
                        }
                        $scope.trackEnhancedEcommerceCheckoutOption(stepIndex, googleAnalytics);
                    }

                    // Check for redirects
                    if (data.redirect) {
                        window.location.href = data.redirect;
                        return;
                    }

                    // Update the model with data
                    $scope.updateModel(data);

                    // Execute the callback, if we have one
                    if (typeof(paramObj.callback) === 'function') {
                        paramObj.callback(data);
                    }

                    $scope.model.stateData.processingRequest = false;

                    if (postMethod === 'applySourceCode') {

                        // CKT7.6 - track virtual pageview when clicking Apply Coupon (Payment Step).
                        // Note that "CKT7.7 - Capture promotion code entered" is prepared at
                        // PaymentService.processSourceCode and only gets tracked in the next page loading.
                        if (data.reporting !== undefined && data.reporting.promotionCode !== undefined) {
                            MarketLive.Events.checkoutPaymentPromoCodeApplied.trigger({
                                promoCode: data.reporting.promotionCode
                            });
                        }
                    }

                    if (data !== undefined && data.reporting !== undefined && data.reporting.data !== undefined) {
                        var reportingData = data.reporting.data;
                        $log.debug(reportingData);

                        if (postMethod !== 'shipToOptionChange' &&
                            postMethod !== 'selectedContactChange' &&
                            postMethod !== 'saveAndContinueAddress' &&
                            postMethod !== 'updateStepData' &&
                            postMethod !== 'applyGiftOptions') {
                            MarketLive.Events.checkoutReportingDataPosted.trigger({reportingData: reportingData});
                        }
                    }

                }, function () {
                    // Log out
                    $log.debug('postData: failed to receive data');
                    $scope.model.stateData.processingRequest = false;
                });
            };

            /**
             * Post the form's data.
             * @param {{event: object, stepIndex: number, method: string, [callback]: function}} paramObj
             */
            $scope.postForm = function (paramObj) {
                var targetForm = angular.element(paramObj.event.target).closest('form:not([target])'),
                    stepIndex = paramObj.stepIndex,
                    postMethod = paramObj.method,
                    isValid;

                // Log out
                $log.debug('-- postForm --');

                // Validate the form
                isValid = $scope.validateForm({'stepIndex':stepIndex, 'targetForm':targetForm});

                if (isValid) {
                    $log.debug('postForm: validation passed');
                    $log.debug('postForm: posting data');

                    $scope.postData({'stepIndex':stepIndex, 'targetForm':targetForm,
                        'method':postMethod, 'callback': paramObj.callback});
                } else {
                    // Stop event propagation & prevent the default behavior
                    event.stopPropagation();
                    event.preventDefault();

                    $log.debug('postForm: validation failed');
                }
            };

            /**
             * Post the dialog's form data, only closing the dialog if there are no validation errors.
             * @param {{event: object, stepIndex: number, method: string}} paramObj
             */
        $scope.postDialogForm = function (paramObj,borderFreeEnable) {
                var event = paramObj.event;

                // Log out
                $log.debug('-- postDialogForm --');

                // Bail out if we're already processing a request
                if ($scope.model.stateData.processingRequest === true) {
                    return false;
                }

                // Stop event propagation & prevent the default behavior
                event.stopPropagation();
                event.preventDefault();

                // Only close the dialog if there are no server-side validation errors
                angular.element.extend(true, paramObj, {'callback': function (data) {
                    var hasErrors = (data.steps && data.steps[paramObj.stepIndex] && data.steps[paramObj.stepIndex].errors);

               // Check targetDataForUpdate for errors
                    if (!hasErrors){
                        hasErrors = $scope.targetDataForUpdateContainsStepErrors(data);
                    }

                    if (hasErrors) {
                        $log.debug('postDialogForm: server validation failed');
                    } else {
                        angular.element(paramObj.event.target).closest('.modal').modal('hide');
                    }
                }});

            //This will reassign the region value as NA if country is US/CA
            if(borderFreeEnable){
                $("#addressBookForm #country").removeAttr("disabled","disabled");
                var country = $("#addressBookForm #country").val();
                if(country == 'US' || country == 'CA'){
                    $("#addressBookForm #region").val("NA");
                }
            }
                // Post the form
                $scope.postForm(paramObj);

            //Again Disable the country field if Post form call fails
            if(borderFreeEnable){
                $("#addressBookForm #country").attr("disabled","disabled");
            }
            };

            /**
             * Checks to see if 'targetDataForUpdate' contains step errors
             * @param {object} data
             * @returns {boolean}
             */
            $scope.targetDataForUpdateContainsStepErrors = function (data){
                var hasErrors = false, i;

                // Only continue if we have 'targetDataForUpdate'
                if (data.targetDataForUpdate) {
                    // Iterate through all the data items
                    for (i=0; i<data.targetDataForUpdate.length; i++) {
                        var dataItem = data.targetDataForUpdate[i];

                        // If we find 'errors' set the hasErrors flag and break out of the loop
                        if (dataItem.path === 'steps' && dataItem.data && dataItem.data.errors){
                            hasErrors = true;
                            break;
                        }
                    }
                }

                return hasErrors;
            };

            /**
             * Update the model, conditionally, with the data provided
             * @param {Object} data
             */
            $scope.updateModel = function (data) {
                // Log out
                $log.debug('-- updateModel --');

                // Update the 'user' model data
                $scope.model.user = (data.user)? data.user : $scope.model.user;

                // Update the 'steps' model data
                $scope.updateSteps(data);

                //show model
                if (data.messages){
                    if(data.messages.borderFreeTimeout || data.messages.borderFreeNoResponse) {
                        $scope.showBorderFreeMessage();
                    }
                }
                // Update the 'payment' model data
                if (data.payment) {
                    $scope.model.payment = data.payment;
                }

                // Update the 'basketItems' model data
                if (data.basketItems) {
                    $scope.model.basketItems = data.basketItems;
                }

                // Only process this if we have shipping data
                if (data.shipping) {
                    $scope.model.shipping = data.shipping;
                    // Extend the data with the new data
//                angular.element.extend(true, $scope.model.shipping, data.shipping);
                }

                // Update targeted model data if we have 'targetDataForUpdate'
                if (data.targetDataForUpdate) {
                    $scope.updateTargetedData(data.targetDataForUpdate);
                }

                $scope.processServerErrors();
            };

            /**
             * Updates targeted model data
             * @param {TargetDataItem[]} targetDataForUpdate
             */
            $scope.updateTargetedData = function (targetDataForUpdate) {
                var targetDataItem, targetObject, isArray = false, i;

                // Log out
                $log.debug('-- updateTargetedData --');

                // Iterate over the list of data to update
                for (i=0; i<targetDataForUpdate.length; i++){
                    // Set the data item
                    targetDataItem = targetDataForUpdate[i];
                    // Find the matching model data by path
                    targetObject = $scope.getPropertyValueFromPath(targetDataItem.path);
                    // Check to see if the 'targetObject' is an array
                    isArray = angular.isArray(targetObject);

                    // If the 'targetObject' is an array and we have 'findBy' criteria, try to find a match and update
                    // the targetObject
                    if (isArray && targetDataItem.findBy) {
                        // Try and find a match within the array of objects
                        var index = $scope.objectArrayFindBy(targetObject, targetDataItem.findBy);

                        // If we found a match, update the targetObject, otherwise set it to undefined
                        if(index !== -1){
                            targetObject = targetObject[index];
                        } else {
                            targetObject = undefined;
                        }
                    }

                    // Only continue to 'extendOrOverrideData' if the targetObject exists
                    if (targetObject !== undefined){
                        $scope.extendOrOverrideData(targetObject, targetDataItem.data, targetDataItem.extend);
                    }
                }
            };

            /**
             * Extend or Override model data (targetData) with new data (newData) based on an 'extendData' boolean
             * @param {object} targetData
             * @param {object} newData
             * @param {boolean} [extendData]
             */
            $scope.extendOrOverrideData = function(targetData, newData, extendData){
                var targetProperty;

                // Log out
                $log.debug('-- extendOrOverrideData --');
                $log.debug('extendOrOverrideData: targetData: ' + targetData);
                $log.debug(targetData);

                // Iterate over the properties in the 'newData' object
                for (targetProperty in newData) {
                    // Don't bother with 'inherited' properties
                    if (newData.hasOwnProperty(targetProperty)) {
                        $log.debug('extendOrOverrideData: targetProperty: ' + targetProperty);

                        // Override or Extend
                        if(typeof(targetData[targetProperty]) !== 'undefined') {
                            // Override
                            if (!extendData || typeof(targetData[targetProperty]) !== 'object') {
                                $log.debug('extendOrOverrideData: overridding: ' + targetProperty);
                                targetData[targetProperty] = newData[targetProperty];
                                // Extend
                            } else if (extendData) {
                                $log.debug('extendOrOverrideData: extending: ' + targetProperty);
                                angular.element.extend(true, targetData[targetProperty], newData[targetProperty]);
                            }
                            // Add
                        } else {
                            $log.debug('extendOrOverrideData: adding: ' + targetProperty);
                            targetData[targetProperty] = newData[targetProperty];
                        }
                    }
                }
            };

            /**
             * Find an object in a given array by a given key/value pair.
             * @param {Array} searchArray
             * @param {{key: string, value: *}} findBy
             * @returns {number} The index position of the matching object in the array.
             */
            $scope.objectArrayFindBy = function(searchArray, findBy) {
                var i, index = -1;

                // Log out
                $log.debug('-- objectArrayFindBy --');

                // Iterate over all items in the array, and if a match is found store the index position
                for (i=0; i<searchArray.length; i++) {
                    var targetItem = searchArray[i];
                    // If the object has the property and its value matches the find by, store the index position
                    if (targetItem[findBy.key] &&
                        targetItem[findBy.key] === findBy.value) {
                        index = i;
                        break;
                    }
                }

                // Log out
                $log.debug('objectArrayFindBy: index: ' + index);

                return index;
            };

            /**
             * Update the 'steps' model data
             * @param {object} data
             */
            $scope.updateSteps = function (data) {
                var i;

                // Only process this if we have steps data
                if (data.steps) {
                    // Extend the steps data with the new data
                    angular.element.extend(true, $scope.model.steps, data.steps);

                    // Iterate over the steps for properties that need to be overridden
                    for (i = 0; i < data.steps.length; i++) {
                        // Override the summary data
                        if (data.steps[i].summary) {
                            $scope.model.steps[i].summary = data.steps[i].summary;
                        }
                    }
                }
            };

            /**
             * Retrieve the model property value for a given path
             * @param {string} path The path to the model data (example: 'shipping.shipments').
             * @returns {*}
             */
            $scope.getPropertyValueFromPath = function (path) {
                var pathSplit = path.split('.'),
                    value, i, pathNode = $scope.model;

                // Catch any errors mapping to model data, so the rest of the code can continue to operate as expected
                try {
                    // Iterate over the split path, setting the value and possible next path node (object)
                    for (i = 0; i < pathSplit.length; i++) {
                        var pathItem = pathSplit[i],
                            pathItemIndex = null;

                        // Handel path items that appear to be arrays differently
                        if (pathItem.indexOf('[') !== -1) {
                            pathItemIndex = parseInt(pathItem.replace(/\D/g, ''));
                            pathItem = pathItem.split('[')[0];
                            value = pathNode[pathItem][pathItemIndex];
                            // Handel all other path items as regular properties
                        } else {
                            value = pathNode[pathItem];
                        }

                        // Set the new path node to match the current value
                        pathNode = value;
                    }
                } catch (e) {
                    // Log out
                    $log.warn('Could not find the model data with the given path: ' + path);
                }

                return value;
            };

            /**
             * Check to see if there are any server-side errors to display, and if so forward on to 'displayServerErrors'
             */
            $scope.processServerErrors = function () {

                // Clear existing server errors
                var errorElements = angular.element('input.ml-csvalidation-error');
                if (errorElements.length > 0){
                    errorElements.keyup();
                }

                var foundErrors = false;

                // Iterate over the steps and check for errors
                for (var i = 0; i < $scope.model.steps.length; i++) {
                    // If we find errors, set 'foundErrors' and make a call to 'displayServerErrors'
                    if ($scope.model.steps[i].errors !== undefined && $scope.model.steps[i].errors !== null) {

                        // Iterate over each error in the 'errors' object
                        var errors = $scope.model.steps[i].errors,
                            error;
                        for (error in errors) {
                            if (errors.hasOwnProperty(error)) {
                                $scope.logStepError({'stepIndex': i, 'message': errors[error]});
                            }
                        }

                        foundErrors = true;
                        $scope.displayServerErrors(i, $scope.model.steps[i].errors);
                    }
                }

                // If we found errors, auto focus on the first failed field
                if (foundErrors) {
                    // Focus on the first failed field
                    angular.element('.ml-csvalidation-error').filter('input, select, textarea').
                        filter(':first').focus().trigger('focusin');
                }
            };

            /**
             * Display server-side errors.
             * @param {number} stepIndex
             * @param {object} errors
             */
            $scope.displayServerErrors = function (stepIndex, errors) {
                var error, targetField;

                // Log out
                $log.debug('-- displayServerErrors --');

                // Iterate over each error in the 'errors' object
                for (error in errors) {
                    if (errors.hasOwnProperty(error)) {
                        // Log out
                        $log.debug(error + ': ' + errors[error]);

                        // Find the field
                        targetField = angular.element('.ml-accordion-steps > li:eq('+ stepIndex +')')
                            .find('[name="' + error + '"]');

                        // Call the 'ClientSideValidate' code to display the error on the field
                        if (targetField.length > 0) {
                            MarketLive.ClientSideValidate.displayErrorOnField(targetField, errors[error]);
                        }
                    }
                }

                // Remove the 'errors' property from the step, so we don't reprocess it
                delete $scope.model.steps[stepIndex].errors;
            };

            /**
             * Check to see if the model data has been initialized.
             * @returns {boolean}
             */
            $scope.isInitialized = function () {
                return $scope.modelInitialized;
            };

            /**
             * Returns the heading for the step at the given 'stepIndex'.
             * @param {number} stepIndex
             */
            $scope.getStepHeading = function (stepIndex) {
                var heading = '';

                if ($scope.model.steps.length > 0){
                    heading = $scope.model.steps[stepIndex].heading;
                }

                return heading;
            };

            /**
             * Returns the summary data for the step at the given 'stepIndex'.
             * @param {number} stepIndex
             * @returns {Array}
             */
            $scope.getStepSummary = function (stepIndex) {
                var summary = [];

                if ($scope.model.steps.length > 0 && $scope.model.steps[stepIndex].summary) {
                    summary = $scope.model.steps[stepIndex].summary;
                }

                return summary;
            };

            /**
             * Check to see if the 'step' at the provided 'stepIndex' is has summary data.
             * @param {number} stepIndex
             * @returns {boolean}
             */
            $scope.stepHasSummaryData = function (stepIndex) {
                var hasSummaryData = false;

                if ($scope.model.steps.length > 0 && $scope.model.steps[stepIndex].summary) {
                    hasSummaryData = true;
                }
                return hasSummaryData;
            };

            /**
             * Returns the alert data for the step at the given 'stepIndex'.
             * @param {number} stepIndex
             * @returns {Array}
             */
            $scope.getStepAlertData = function (stepIndex) {
                var alerts = [];

                if ($scope.model.steps.length > 0 && $scope.model.steps[stepIndex].alerts) {
                    alerts = $scope.model.steps[stepIndex].alerts;
                }

                return alerts;
            };

            /**
             * Check to see if the 'step' at the provided 'stepIndex' is has alert data.
             * @param {number} stepIndex
             * @returns {boolean}
             */
            $scope.stepHasAlertData = function (stepIndex) {
                var hasAlertData = false;

                if ($scope.model.steps.length > 0 && $scope.model.steps[stepIndex].alerts) {
                    hasAlertData = true;
                }
                return hasAlertData;
            };

            /**
             * Returns the user string from the model data or an empty string if none exists.
             * @returns {string}
             */
            $scope.getUser = function () {
                var user = '';

                if ($scope.model.user.length > 0){
                    user = $scope.model.user;
                }

                return user;
            };

            /**
             * Check to see if the 'step' at the provided 'stepIndex' is collapsed.
             * @param {number} stepIndex
             * @returns {boolean}
             */
            $scope.isStepCollapsed = function (stepIndex) {
                var collapsed = true;

                if ($scope.model.steps.length > 0){
                    collapsed = $scope.model.steps[stepIndex].collapsed;
                }

                return collapsed;
            };

            /**
             * Check to see if the 'step' at the provided 'stepIndex' is accessible.
             * @param {number} stepIndex
             * @returns {boolean}
             */
            $scope.isStepAccessible = function (stepIndex) {
                var accessible = false;

                if ($scope.model.steps.length > 0){
                    accessible = $scope.model.steps[stepIndex].accessible;
                }

                return (accessible);
            };

            /**
             * Check to see if the 'step' at the provided 'stepIndex' is user collapsible.
             * @param {number} stepIndex
             * @returns {boolean}
             */
            $scope.isUserCollapsible = function (stepIndex) {
                var collapsible = false;

                if ($scope.model.steps.length > 0){
                    collapsible = $scope.model.steps[stepIndex].userCollapsible;
                }

                return collapsible;
            };

            /**
             * Check to see if the 'step' at the provided 'stepIndex' is hidden.
             * @param {number} stepIndex
             * @returns {boolean}
             */
            $scope.isStepHidden = function (stepIndex) {
                var hidden = false;

                if ($scope.model.steps.length > 0){
                    hidden = $scope.model.steps[stepIndex].hidden;
                }

                return hidden;
            };

            /**
             * Check to see if the 'step' at the provided 'stepIndex' is completed.
             * @param {number} stepIndex
             * @returns {boolean}
             */
            $scope.isStepCompleted = function (stepIndex) {
                var completed = false;

                if ($scope.model.steps.length > 0){
                    completed = $scope.model.steps[stepIndex].completed;
                }

                return completed;
            };

            /**
             * Expand (mark as collapsed=false) the 'step' at the provided 'stepIndex'.
             * @param {number} stepIndex
             */
            $scope.toggleStepCollapse = function (stepIndex) {
                var currentStep /** @type {Step} */,
                    i;

                // Log out
                $log.debug('-- toggleStepCollapse --');

                if ($scope.model.steps.length > 0) {
                    currentStep = $scope.model.steps[stepIndex];

                    var allow = false;
                    if (currentStep.collapsed){
                        // allow expand
                        allow = true;
                    }else{
                        // allow collapse
                        allow = currentStep.userCollapsible;
                    }

                    if (currentStep.accessible && allow) {
                        // Collapse all other steps
                        for (i = 0; i<$scope.model.steps.length; i++){
                            if (i !== stepIndex){
                                $scope.model.steps[i].collapsed = true;
                            }

                            // Disable all steps greater than the current 'stepIndex'
                            if (i > stepIndex) {
                                $scope.model.steps[i].accessible = false;
                                $scope.model.steps[i].completed = false;
                            }
                        }

                        // Expand the current Step
                        currentStep.collapsed = !currentStep.collapsed;
                    }
                }
            };

            /**
             * Check to see if we should show the 'Additional Addresses'.
             * @returns {boolean}
             */
            $scope.showAdditionalAddresses = function () {
                var showAdditionalAddresses = false;

                if ($scope.model.shipping.shipTo && $scope.model.shipping.shipTo.selectedOption){
                    showAdditionalAddresses = ($scope.model.shipping.shipTo.selectedOption !== 'SHIPTO_BILLING');
                }

                return showAdditionalAddresses;
            };

            /**
             * Show the New Address Form, but only after resetting the validation.
             * @param {object} event
             * @param {number} stepIndex
             */
            $scope.showNewAddressForm = function (event, stepIndex,borderFreeEnable,countrySel) {
                var addressBookModal = angular.element('#addressBookModal'),
                    targetForm = addressBookModal.find('form:first');

                // Log out
                $log.debug('-- showNewAddressForm --');

                // Stop event propagation & prevent the default behavior
                event.stopPropagation();
                event.preventDefault();

                // Reset the validation
                $scope.resetValidation({'stepIndex':stepIndex, 'targetForm':targetForm });

                /**
                 * pre-fill socialProfile into the new address form
                 */
                $('#addressBookForm #firstName').val($scope.model.socialProfile.firstName);
                $('#addressBookForm #lastName').val($scope.model.socialProfile.lastName);
                $('#addressBookForm #emailAddress').val($scope.model.socialProfile.email);
                $('#addressBookForm #dayPhone').val($scope.model.socialProfile.phoneNumber);
                $('#addressBookForm #city').val($scope.model.socialProfile.city);
                $('#addressBookForm #streetAddress').val($scope.model.socialProfile.streetAddress);

                // Show the dialog
                //addressBookModal.modal('show');

                // Make the call to get the new addressform reporting data
                $scope.getData({params:{method:'getNewAddressFormReportingData'}, callback:function(data){

                    addressBookModal.modal('show');

                    if (data.reporting !== undefined && data.reporting.data !== undefined) {
                        var reportingData = data.reporting.data;
                        $log.debug(reportingData);

                        MarketLive.Events.checkoutReportingDataPosted.trigger({reportingData: reportingData});
                    }

                }});
                if(borderFreeEnable){
                $("#addressBookForm #country > option[ value = " + countrySel + "]").attr('selected', "true");
                $("#addressBookForm #country").val(countrySel);
                    if(countrySel.match('US|CA')){
                        $("#addressBookForm #boderFreeregionMenuState").removeClass("ng-hide");
                        $("#addressBookForm #boderFreeregionMenuState").addClass("ng-show");
                        $("#addressBookForm #boderFreeregionTextState").removeClass("ng-show");
                        $("#addressBookForm #boderFreeregionTextState").addClass("ng-hide");
                    }
                    $("#addressBookForm #country").attr("disabled","disabled");

                    //This will remove all the CA states and NA/NA options from select drop down
                    $("#addressBookForm #state > option").each(function() {
                        var stateVal= this.value;
                        var stateArr = stateVal.split("/");
                        if( stateArr[0] === 'CA' || stateVal === 'NA/NA'){
                            $("#addressBookForm #state > option[value='" + stateVal + "']").remove();
                        }
                    });
                }
            };


            /**
             * Show the show gift options, but only after getting the gift options data
             * @param {{event: object, type: string, shipment: object}} paramObj
             */
            $scope.showGiftOptions = function (paramObj) {
                var event = paramObj.event,
                    type =  paramObj.type,
                    shipment = paramObj.shipment,
                    id = shipment.id,
                    targetModel = angular.element(event.target).closest('.ml-accordion-step')
                        .find('.ml-gift-option-modal:first');

                // Stop event propagation & prevent the default behavior
                event.stopPropagation();
                event.preventDefault();

                // Log out
                $log.debug('-- showGiftOptions --');
                $log.debug('showGiftOptions: type: ' + type);
                $log.debug('showGiftOptions: id: ' + id);

                // Make the call to get the gift options
                $scope.getData({params:{method:'getGiftOptions', 'type':type, 'id':id}, callback:function(data){
                    // Set the 'activeShipment'
                    $scope.model.stateData.activeShipment = shipment;

                    // Show the gift options dialog
                    targetModel.modal('show');

                    MarketLive.Events.checkoutGiftOptionsClicked.trigger();

                    if (data.reporting !== undefined && data.reporting.data !== undefined) {
                        var reportingData = data.reporting.data;
                        $log.debug(reportingData);

                        MarketLive.Events.checkoutReportingDataPosted.trigger({reportingData: reportingData});
                    }

                }});
            };

            /**
             Border Free error message dialog
             */
            $scope.showBorderFreeMessage = function () {
            angular.element('a.openModal').click();
            };

            /**
             * Returns the step index for a step with a given id.
             * @param {string} id
             * @returns {number}
             */
            $scope.getStepIndexById = function (id) {
                var stepIndex = -1, i;

                if ($scope.model.steps && $scope.model.steps.length > 0){
                    for (i = 0; i < $scope.model.steps.length; i++) {
                        if ($scope.model.steps[i].id === id) {
                            stepIndex = i;
                            break;
                        }
                    }
                }

                return stepIndex;
            };

            $scope.logStepSuccess = function (paramObj) {

                if (MarketLive.Events) {
                    var stepId = $scope.model.steps[paramObj.stepIndex].id;
                    switch(stepId)
                    {
                        case 'bill': // Billing
                            MarketLive.Events.checkoutBillingContinueClicked.trigger();

                            // email sign up
                            var emailSignUp = $('input[name="emailSignup"]');
                            if (emailSignUp.length > 0 && emailSignUp.is(':checked')) {
                                MarketLive.Events.checkoutBillingEmailSignUpSelected.trigger();
                            }
                            break;

                        case 'ship': // Shipping
                            if ($scope.model.shipping.shipTo && $scope.model.shipping.shipTo.selectedOption){

                                var shippingOption = $scope.model.shipping.shipTo.selectedOption;
                                MarketLive.Events.checkoutShippingContinueClicked.trigger({
                                    shippingOption: shippingOption
                                });
                            }
                            break;

                        case 'pay': // Payment
                             var amazonMethodUsed = false;
                             var visaMethodUsed = false;
                             // Check if payment is done using Amazon widget
                             if($(".ml-amazon-widget-content").attr("id")){
                                 amazonMethodUsed = true;
                             }

                            // check PayPal is enabled or not
                            var paypalMethodUsed = false;
                            var paymentTypes = $('input[name="paypalCheckoutSelected"]:checked');
                            if (paymentTypes !== null && paymentTypes !== 'undefined' &&
                                paymentTypes.length > 0 && paymentTypes.is(':checked')) {
                                if('YES' === paymentTypes.val()) {
                                    paypalMethodUsed = true;
                                } else if('VISA_CHECKOUT_YES' === paymentTypes.val()) {
                                    visaMethodUsed = true;
                                }
                            }
                            // CKT7.1, CKT7.2, CKT19.1 - track Paypal or Credit Card method used
                            MarketLive.Events.checkoutPaymentTypeSelected.trigger({
                                paypalMethodUsed: paypalMethodUsed,
                                amazonMethodUsed: amazonMethodUsed,
                                visaMethodUsed: visaMethodUsed
                            });
                            break;
                    }
                }
            };

            $scope.logStepError = function (paramObj) {

                if (MarketLive.Events) {
                    var stepId = $scope.model.steps[paramObj.stepIndex].id,
                        message = paramObj.message;
                    switch(stepId)
                    {
                        case 'bill': // Billing
                            MarketLive.Events.checkoutBillingErrorOccurred.trigger({
                                message: message
                            });
                            break;

                        case 'ship': // Shipping
                            MarketLive.Events.checkoutShippingErrorOccurred.trigger({
                                message: message
                            });
                            break;

                        case 'pay': // Payment
                            MarketLive.Events.checkoutPaymentErrorOccurred.trigger({
                                message: message
                            });
                            break;
                    }
                }
            };

            // Watch the 'shipTo.selectedOption' for changes and act accordingly
            $scope.$watch('model.shipping.shipTo.selectedOption', function(newValue, oldValue) {
                if (newValue !== undefined && oldValue !== undefined && newValue !== oldValue) {

                    // Hide/show the Delivery step
                    if ($scope.model.stateData.deliveryStepIndex !== -1) {
                        $scope.model.steps[$scope.model.stateData.deliveryStepIndex].hidden = (newValue !==
                            'SHIPTO_MULTIPLE');
                    }

                    // Post data on change
                    if (oldValue !== undefined) {
                        $scope.postData(
                            {
                                method:'shipToOptionChange',
                                data: 'shippingStyle='+ encodeURIComponent(newValue)
                            }
                        );
                    }
                }
            });

            // Watch the 'addresses.selectedOption' for changes and act accordingly
            $scope.$watch('model.shipping.addresses.selectedOption', function(newValue, oldValue) {
                if (newValue !== undefined && oldValue !== undefined &&
                    $scope.model.shipping.shipTo.selectedOption === 'SHIPTO_SINGLE') {
                    // Post data on change
                    $scope.postData(
                        {
                            method:'selectedContactChange',
                            data:'selectedContact='+encodeURIComponent(newValue)
                        }
                    );
                }
            });

            // Watch 'processingRequest' for changes and block/unblock the ui when its value is true/false respectively.
            $scope.$watch('model.stateData.processingRequest', function(newValue, oldValue) {
                if (newValue !== undefined && oldValue !== undefined && newValue !== oldValue) {
                    if (newValue === true) {
                        MarketLive.Base.blockUI({fadeIn: 0});
                    } else {
                        MarketLive.Base.unblockUI();
                    }
                }
            });

             /**
             * Check to see if we should show the 'Region feild'.
             * @param {string} borderFreeEnable
             * @param {string} borderFreeOrder
             * @param {string} countrySelected
             * @returns {boolean}
             */
            $scope.showBorderFreeRegion = function (borderFreeEnable,borderFreeOrder,countrySelected, path) {
                var showBorderFreeRegionResult = false;
                if (borderFreeEnable){
                    var regionValShipping= $(path).val();
                    if( countrySelected != undefined && null != countrySelected && countrySelected.length > 0 ){
                    if(borderFreeOrder){
                        showBorderFreeRegionResult = true;
                            if(!countrySelected.match("US|CA")){
                                showBorderFreeRegionResult = true;
                            }else {
                                showBorderFreeRegionResult = false;
                                if(regionValShipping  === ''){
                                    $(path).val('NA');
                                }
                            }
                    }else{
                        //if user refresh the browser
                            if(!countrySelected.match("US|CA|''")){
                            showBorderFreeRegionResult = true;
                            if(regionValShipping  === 'NA'){
                                $(path).val('');
                            }
                        }
                    }
                    }else{
                        showBorderFreeRegionResult = false;
                    }
                }
                return showBorderFreeRegionResult;
            };

             /**
             * Check to see if we should show the 'State field'.
             * @param {string} borderFreeEnable
             * @param {string} borderFreeOrder
             * @param {string} countrySelected
             * @returns {boolean}
             */
            $scope.showBorderFreeStateMenu = function (borderFreeEnable,borderFreeOrder,countrySelected, path, countryPathInForm) {
                var showBorderFreeStateMenuResult = false;
                if (borderFreeEnable){
                    var countryInForm = $(countryPathInForm).val();
                    var stateValShipping= $(path).val();
                    if( countrySelected != undefined && null != countrySelected && countrySelected.length > 0 ){
                    if(borderFreeOrder){
                        showBorderFreeStateMenuResult = false;
                            if(countrySelected.match("US|CA")){
                                showBorderFreeStateMenuResult = true;
                                if(stateValShipping !== ''){
                                    if(stateValShipping === 'NA/NA'){
                                        $(path).val('');
                                    }else{
                                        $(path).val(stateValShipping);
                                    }
                                }
                            }else{
                                if(countryInForm != undefined && countryInForm != '' && !countryInForm.match('US|CA')) {
                                    $(path).val('NA/NA');
                                }
                                showBorderFreeStateMenuResult = false;
                            }
                    }else{
                        if(stateValShipping === 'NA/NA' || stateValShipping === ''){
                            if(countrySelected.match('US|CA') && countryInForm != undefined && countryInForm != '' && countryInForm.match('US|CA')){//if ajax request for validation we need to check selected form country also before resetting
                                $(path).val('');
                            }else{
                                $(path).val('NA/NA');
                            }
                        }else{
                           $(path).val(stateValShipping);
                        }
                        showBorderFreeStateMenuResult = true;
                        //if user refresh the browser
                        if(!countrySelected.match("US|CA")){
                            showBorderFreeStateMenuResult=false;
                        }
                    }
                }else{
                    showBorderFreeStateMenuResult = true;
                }
                }else{
                    showBorderFreeStateMenuResult = true;
                }

                return showBorderFreeStateMenuResult;
            };
         /**
             * Verify the country and set it accordingly.
         * @param {string} countrySelected
         * @param {string} path
             * @returns {boolean}
             */
            $scope.verifyBorderFreeCountry = function (countrySelected, path) {
                var countryCode= $(path).val();
                if(countryCode !== undefined && countryCode !== countrySelected) {
                    $(path).val(countrySelected);
                }
                return true;
            };

            /**
             * Check to see if we should show the 'Zip Postal Message'.
             * @param {string} borderFreeEnable
             * @param {string} countrySelected
             * @returns {boolean}
             */
            $scope.showZipPostalMessage = function (borderFreeEnable,countrySelected){
                var showZipPostalMessage = true;
                if(borderFreeEnable){
                    if(!countrySelected.match("US|CA|''")){
                        showZipPostalMessage = false;
                    }else{
                        showZipPostalMessage = true;
                    }
                }
                return showZipPostalMessage;
            };

            /**
             * Measure Google Analytics Enhanced Ecommerce checkout options.
             * @param {number} stepIndex
             */
            $scope.trackEnhancedEcommerceCheckoutOption = function (stepIndex, googleAnalytics) {
                // Measuring Google Analytics Enhanced Ecommerce checkout options
                // Only execute this block if we have a 'stepIndex'
                if (stepIndex !== undefined) {

                    // track shipping option and payment option only
                    var stepId = $scope.model.steps[stepIndex].id;                   
                    var trackDeliveryStep = false;
                    if (stepId === 'ship' || stepId === 'pay') {

                        var optionLabel = '',
                            selectedValue = '',
                            optionList = [],
                            skipOptionLabelRetrieval = false;
                        if (stepId === 'ship' &&
                            $scope.model.shipping.shipTo &&
                            $scope.model.shipping.shipTo.selectedOption){

                            // measure shipping option {billing address, different address, more than one address}
                            selectedValue = $scope.model.shipping.shipTo.selectedOption;
                            optionList = $scope.model.shipping.shipTo.options;

                            if (selectedValue !== 'SHIPTO_MULTIPLE') {
                                trackDeliveryStep = true;
                            }

                        } else if (stepId === 'pay') {

                            var paypalSelected = ($scope.model.payment.paypalCheckoutSelected === 'YES');
                            if (paypalSelected) {
                                optionLabel = 'Paypal';
                                skipOptionLabelRetrieval = true;

                            } else if($scope.model.payment.paypalCheckoutSelected === 'AFFIRM_YES') {
                            	optionLabel = 'Affirm';
                                skipOptionLabelRetrieval = true;
                            	} 
		                         else if ($scope.model.payment.creditCards &&
		                              
		                        		 $scope.model.payment.creditCards.selectedOption) {
		                                // measure payment option {Paypal, Visa, Mastercard, Discover, American Express}
		                                selectedValue = $scope.model.payment.creditCards.selectedOption;
		                                optionList = $scope.model.payment.creditCards.options;
		                            }
                        }

                        // get the option label from value
                        if (!skipOptionLabelRetrieval) {
                            for (var i = 0; i < optionList.length; i++) {
                                var optionObj = optionList[i];
                                if (optionObj.value === selectedValue) {
                                    optionLabel = optionObj.label;
                                    break;
                                }
                            }
                        }

                        // Check if payment is done using Amazon widget
                        if($(".ml-amazon-widget-content").attr("id")){
                            if (stepId === 'ship') {
                                optionLabel = 'single address';
                            }
                            else if (stepId === 'pay') {
                                optionLabel = 'Amazon';
                            }
                        }

                        MarketLive.Events.enhancedEcommerceCheckoutOptionSelected.trigger(
                            {stepId: stepId, option: optionLabel});

                        if (trackDeliveryStep) {
                            if (googleAnalytics && googleAnalytics.enhancedEcommerce) {
                                googleAnalytics.enhancedEcommerce.virtualStep = '4';
                            }
                        }
                    }
                }
            };

            // Call to init the model data
            $scope.init();
        }]);
}(window.angular));
