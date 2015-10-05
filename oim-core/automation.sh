#!/bin/sh
CLASSPATH="/home/amit-yadav/workspace/isource-cm/oim-core/target/salesmachine-core-jar-with-dependencies.jar"

export CLASSPATH

echo "logs being created at logs/automation.log"

nohup java -Xms64m -Xmx1028m salesmachine.automation.AutomationManager 1>/dev/null 2>&1 &
echo "CM Script"
#echo $output 
nohup java -Xms64m -Xmx1028m salesmachine.automation.AutomationManager stopped 1>/dev/null 2>&1 &
