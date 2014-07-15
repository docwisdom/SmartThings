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
 *
 *  --This SmartApp has the intention of notifying you of traffic conditions on your Hue bulbs and alerting you of departure time
 *  --based on that traffic. The app will request two locations, the expected time of arrival, and when to start polling for traffic.
 *  --It will also allow you to set the thresholds for traffic and what colors to change the Hue to.
 *
 *  --Special thanks to scottinpollock for code examples
 *
 *
 *  if realTime > time
 *  if (arrivalTime - realTime) >= now
 */

import groovy.json.JsonSlurper


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
    section("Control these bulbs:") {
		input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
	}
    //color for no traffic
	section("Color For No Traffic:"){
		input "color1", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel1", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
    //some traffic threshold in minutes
	section("Traffic delay over this many minutes is considered Some Traffic:") {
		input "threshold2", "number", title: "Minutes?"
	}
    //color for some traffic
    section("Color For Some Traffic:"){
		input "color2", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel2", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
    //bad traffic threshold in minutes
	section("Traffic delay over this many minutes is considered Bad Traffic:") {
		input "threshold3", "number", title: "Minutes?"
	}
    //color for bad traffic
    section("Color For Bad Traffic:"){
		input "color3", "enum", title: "Hue Color?", required: false, multiple:false, options: [
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
    def todayFormatted = new Date().format( "M-d-yyyy")
    log.debug "Today is = ${todayFormatted}"

    def leaveTime = seconds_to_hhmmss(realTime)
    log.debug "Leave Time is = ${leaveTime}"
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
            'timeType': '3',
            'dateType': '0',
            'date': '${todayFormatted}',
            'localTime': '${arrivalTime}',
            ]
	]

    httpPost(params) {response ->

        	//def map = [:]
           // def descMap = parseDescriptionAsMap(returnedResponse)
           // def body = new String(descMap["body"])
           // def slurper = new JsonSlurper()
           // def result = slurper.parseText(body)

           // if (result.containsKey("realTime")){
           //     def realTime = result.realTime
           // }


           // def parseDescriptionAsMap(description) {
            //description.split(",").inject([:]) { map, param ->
                //def nameAndValue = param.split(":")
                //map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
                //}
            //}
        if(method != null) {

      	}
        return result
    }
}

def seconds_to_hhmmss(sec) {
    new GregorianCalendar(0, 0, 0, 0, 0, sec, 0).time.format('HH:mm:ss')
}
def hhmmss_to_seconds(hhmmss) {
    (Date.parse('HH:mm:ss', hhmmss).time - Date.parse('HH:mm:ss', '00:00:00').time) / 1000
}
