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
import groovy.time.*


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
		input "departFrom", "text", title: "Address?"
	}
    //what is the destination location?
	section("Arriving At:"){
		input "arriveAt", "text", title: "Address?"
	}
    //what time do you need to arrive?
	section("Expected Arrival Time:"){
		input "arrivalTime", "time", title: "When?"
	}
    // //what time should I begin checking traffic?
	section("Begin Checking At:"){
		input "checkTime", "time", title: "When?"
	}
    /**
    //which hue bulbs to control?
    section("Control these bulbs:") {
		input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
	}
    //color for no traffic
	//section("Color For No Traffic:"){
		input "color1", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
		//input "lightLevel1", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
	}
    */
    //some traffic threshold in minutes
	section("Traffic delay over this many minutes is considered Some Traffic:") {
		input "threshold2", "number", title: "Minutes?"
	}
    /**
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
    */
    //bad traffic threshold in minutes
	section("Traffic delay over this many minutes is considered Bad Traffic:") {
		input "threshold3", "number", title: "Minutes?"
	}
    /*
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
    */
}

//init upon install
def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

//reinit upon update
def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

//main script upon init
def initialize() {
	def tz = TimeZone.getTimeZone('PST')
	def formattedNow = new Date().format("HH:mm:ss", tz)
    def todayFormatted = new Date().format("MM/dd/yyyy")
    def arrivalTimeFormatted = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSX", arrivalTime).format('HH:mm', tz)
    def departFromFormatted = URLEncoder.encode(departFrom.toString(), "UTF-8")
    def arriveToFormatted = URLEncoder.encode(arriveAt.toString(), "UTF-8");

    log.debug "The time right now is ${formattedNow}"
    log.debug "Todays date is ${todayFormatted}"
    log.debug "Unformatted arrival time is ${arrivalTime}"
    log.debug "Expected Arrival Time is ${arrivalTimeFormatted}"
    log.debug "The departure is ${departFromFormatted}"
    log.debug "The arrival is ${arriveToFormatted}"

	if(now() > timeToday(checkTime).time && now() < timeToday(arrivalTime).time){
        checkTrafficHandler(departFromFormatted, arriveToFormatted, todayFormatted, arrivalTimeFormatted)
    }

}

//handles the traffic API call from Mapquest and calcualtes traffic time
def checkTrafficHandler(departFromFormatted, arriveToFormatted, todayFormatted, arrivalTimeFormatted) {
    log.debug "formatted variables are ${departFromFormatted} ${arriveToFormatted} ${todayFormatted} ${arrivalTimeFormatted}"

    // Connect to mapquest API
    try{
        httpGet("http://www.mapquestapi.com/directions/v2/route?key=Fmjtd%7Cluur20u82u%2Can%3Do5-9ay506&from=${departFromFormatted}&to=${arriveToFormatted}&narrativeType=none&ambiguities=ignore&routeType=fastest&unit=m&outFormat=json&useTraffic=true&timeType=3&dateType=0&date=${todayFormatted}&localTime=${arrivalTimeFormatted}") {resp ->
        if (resp.data) {
        	//debugEvent ("${resp.data}", true)
            def actualTime = resp.data.route.realTime.floatValue()
            def expectedTime = resp.data.route.time.floatValue()
            log.debug "Actual time ${actualTime} and expected time ${expectedTime}"
            }
            if(resp.status == 200) {
            log.debug "poll results returned"
        }
            else {
            log.error "polling children & got http status ${resp.status}"
        }
    }
    } catch(Exception e)
    {
      log.debug "___exception polling children: " + e
        debugEvent ("${e}", true)
    }


    /*
 	//if the actual travel time exceeds the expected time plus bad traffic threshold
	def threshold3Seconds = threshold3 * 60
    log.debug threshold3Seconds
 	if (actualTime > (expectedTime + (threshold3 * 60))) {
    	debug.log "Do RED!"
    }
    //if the actual travel time exceeds the expected time plus some traffic threshold
    else if (actualTime > (expectedTime + (threshold2 * 60))) {
    	debug.log "Do YELLOW!"
    }
    else {
    	debug.log "Do GREEN!"
    }
*/
}

def seconds_to_hhmmss(sec) {
    new GregorianCalendar(0, 0, 0, 0, 0, sec, 0).time.format('HH:mm:ss')
}
def hhmmss_to_seconds(hhmmss) {
    (Date.parse('HH:mm:ss', hhmmss).time - Date.parse('HH:mm:ss', '00:00:00').time) / 1000
}

