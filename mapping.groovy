//http://www.mapquestapi.com/directions/v2/route?key=Fmjtd%7Cluur20u82u%2Can%3Do5-9ay506&from=6706%20hwy%209,%20felton,%20ca%2095018&to=400%20beach%20street,%20santa%20cruz,%20ca&narrativeType=none&ambiguities=ignore&routeType=fastest&unit=m&outFormat=json&useTraffic=true&timeType=1

 //   if realTime > time

/**
 *  Traffic Report
 *
 *  Copyright 2014 Brian Critchlow
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: "Traffic Report",
    namespace: "docwisdom",
    author: "Brian Critchlow",
    description: "notifies of traffic conditions by Hue color and flashes when you should leave based on set arrival time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/docwisdom-smartthings/Transport-traffic-jam-icon.png",
    iconX2Url: "https://s3.amazonaws.com/docwisdom-smartthings/Transport-traffic-jam-icon.png")


preferences {
	//what is the departure location?
	section("Departing From:"){
		input "from", "text", title: "Address?"
	}
    //what is the destination location?
	section("Arriving At:"){
		input "to", "text", title: "Address?"
	}
    //what time do you need to arrive?
	section("Expected Arrival Time:"){
		input "arrivalTime", "time", title: "When?"
	}
    // //what time should I begin checking traffic?
	section("Begin Checking At:"){
		input "checkTime", "time", title: "When?"
	}
    //which hue bulbs to control?
    section("Control these bulbs...") {
		input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
	}
    //first threshold in minutes
	section("When Commute Is Normal") {
		input "threshold1", "number", title: "Minutes?"
	}
    //first color for threshold 1
	section("Color For No Traffic:"){
		input "color1", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel1", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
    //second threshold in minutes
	section("Some Traffic Adds This Many Minutes To Commute:") {
		input "threshold1", "number", title: "Minutes?"
	}
    //second color for threshold 2
    section("Color For Some Traffic:"){
		input "color2", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel2", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
    //second threshold in minutes
	section("Bad Traffic Adds This Many Minutes To Commute:") {
		input "threshold1", "number", title: "Minutes?"
	}
    //third color for threshold 3
    section("Color For Bad Traffic:"){
		input "color1", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel3", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
}


def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    //checkTrafficHandler()
}

def checkTrafficHandler(evt) {
	log.debug "Event = $evt"

    // Connect to mapquest API
	def params = [
        uri: "http://www.mapquestapi.com",
        path: "/directions/v2/route?",
        headers: ['Cache-Control': 'no-cache', 'Content-Type': 'application/x-www-form-urlencoded'],
        body: [
        	'key': 'Fmjtd%7Cluur20u82u%2Can%3Do5-9ay506',
            'from': '${from}',
            'to': '${to}',
			'narrativeType': 'none',
            'ambiguities': 'ignore',
            'routeType': 'fastest',
            'unit': 'm',
            'outFormat': 'json',
            'useTraffic': 'true',
            'timeType': '1',
            ]
	]

    httpPost(params) {response ->

    	if(method != null) {
        	api(method, args, success)
      	}
        return result
    }
}
