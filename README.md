# PCF Hyperic plugin Build

1. Clone this repository and `cd` into it and execute:
```
  mvn package
```
  The jar will build in target [local git repo]/target/PCF-plugin.jar
  
  Specific tests in this build connect to the Ops Metric server, VPN is required.

# PCF Hyperic plugin Install

This plugin will auto discover PCF metric endpoints via the Pivotal Ops Metrics tile and allow uplink into Hyperic for collection.

Simplified Setup Instructions:

1. Install Hyperic Agent on Linux VM/Host

2. Register Hyperic Agent with the Hyperic Server

3. Copy the plugin jar ( PCF-plugin.jar ) to the following locations:

    server:`[hyperic root]/hq-engine/hq-server/webapps/ROOT/WEB-INF/hq-plugins`

    client:`[hyperic agent root]/agent-#.#.#-EE/bundles/agent-x86-64-linux-#.#.#/pdk/plugins`


4. Restart the Hyperic Agent

5. Navigate to the Hyperic Dashboard & Accept the Discovery of the "Pivotal CloudFoundry 1.4.x" Server Object associated with the Linux Agent Platfrom.


Additional KPIs can be added by editing the etc/hq-plugin.xml, and ensuring the query strings match according to the JMX properties exposed by Ops Metrics.

* Currently Pivotal Ops Metrics does not support external connections of DNAT from the Hyperic Agent.
