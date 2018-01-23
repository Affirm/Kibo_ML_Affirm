window.MarketLive = window.MarketLive || {};
MarketLive.P2P = MarketLive.P2P || {};
MarketLive.P2P.Affirm = MarketLive.P2P.Affirm || {};


//Create members for MarketLive.P2P.Basket namespace
(function(ns, $) {

    'use strict';
     var affirmLogo = '<img src="https://cdn-assets.affirm.com/images/blue_logo-solid_bg.svg" style="height:1em; margin:0 .3em .15em;vertical-align:bottom;">';
     var affirmMessage = "Starting at $ dollarsParam a month with " + affirmLogo +" Learn More";
     var index = 0;
     
    ns.onMonthlyPaymentReady = function(aprLoan, months, message, minRangePrice, maxRangePrice, siteCurrency){
    	$(document).ready(function(){

    		 if(minRangePrice == undefined){
    		   		minRangePrice = 5000
    		   	  }
    		   	  if(maxRangePrice == undefined) {
    		   		  maxRangePrice = 1750000;
    		   	  }
    		 affirm.ui.ready(
    				    function() {
    				      var estimates = document.getElementsByClassName("learn-more");
    				      for (var i = estimates.length - 1; i >= 0; i--) {
    				        var _estimate = estimates[i];
    				        ns.updateAffirmAsLowAs(_estimate.dataset.affirmprice,_estimate, aprLoan,months, message, minRangePrice, maxRangePrice,siteCurrency);
    				      };
    				    } 
    				  ); // change to your template value for product or cart price
    	})
    }
    
   ns.updateAffirmAsLowAs = function ( amount, _estimate, aprLoan, months, message,minRangePrice,maxRangePrice, siteCurrency){
	   		if(message != undefined){
	   			affirmMessage = message;
	   		}
	   		
	   	  var formattedAmount = ns.formatAffirmAmount(amount, siteCurrency);
	   	  
    	  // Only display as low as for items over $50 and less than $17500
    	  if ( ( formattedAmount == null ) || ( formattedAmount < minRangePrice ) || (formattedAmount > maxRangePrice) ) { return; } 
    	  
    	  // Define payment estimate options
    	  var options = {
    	    apr: aprLoan , // percentage assumed APR for loan
    	    months: months, // can be 3, 6, or 12
    	    amount: formattedAmount // USD cents
    	  };
    	  
    	  // request a payment estimate
    	  function handleEstimateResponse(payment_estimate) {
    	      var dollars = payment_estimate.payment_string;
        
    	      var content = affirmMessage.replace("dollarsParam",dollars);
   	    	  _estimate.innerHTML = content;
   	    	  _estimate.onclick = payment_estimate.open_modal;
   	    	  _estimate.style.visibility = "visible";
    	    
    	  };
    	  
    	  affirm.ui.payments.get_estimate(options, handleEstimateResponse);
   }
   
   ns.formatAffirmAmount = function(amount, siteCurrency){
	   
	   var newAmount = amount.replace('.','');
	   return newAmount.replace(siteCurrency,'');
   }
	 
})(MarketLive.P2P.Affirm, jQuery);