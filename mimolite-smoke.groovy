/**
 *  Fortrezz MIMOlite
 *
 *  Author: Based on SmartThings code
 *  Date: 2014-03-6
 */

// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "MIMOlite Smoke", author: "carlos@carlossaldarriaga.com") {
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Contact Sensor"

	}

	// simulator metadata
	simulator {
    	status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

		// status messages
		status "open":  "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
	}

	// UI tile definitions
	tiles {
        standardTile("contact", "device.contact", width: 2, height: 2, inactiveLabel: false) {
			state "open", label: "clear", icon: "st.alarm.smoke.smoke"
			state "closed", label: "smoke", icon: "st.alarm.smoke.smoke", backgroundColor: "#ffa81e"
		}
        standardTile("switch", "device.switch", canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.alarm.alarm", backgroundColor: "#FF0000"
			state "off", label: '${name}', action: "switch.on", icon: "st.alarm.alarm", backgroundColor: "#ffffff"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("alarm", "device.alarm", inactiveLabel: false) {
			state "alarm", label:'${currentValue}'
		}


		main (["switch", "contact"])
		details(["switch", "contact", "refresh", "alarm"])
	}
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def sensorValueEvent(Short value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is clear")
	} else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName detects smoke")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def poll() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

def refresh() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}
