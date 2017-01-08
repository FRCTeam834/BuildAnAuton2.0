# BuildAnAuton_2.0
The necessary jars can be found in the downloads folder.

Instructions for setup:
<ol>
<li>Put the jar in the same folder as the WPILib jars. This is usually found in [User]/wpilib/java/current/lib. </li>
<li>Make your robot project. </li>
<li>Add the jar to your robot projects build path. For eclipse, go to Properties -> Java Build Path -> Add External Jar and select the downloaded jar. </li>
<li>Go into build.properties and in the userLibs property put in the path of the jar that you just moved. The entire line should be userLibs=[User]/wpilib/java/current/lib/BuildAnAuton.jar. I know that there is a comment saying not to put the jar in that folder, but I'm not sure how to work around this. So, for now, if there is a plugin update you'll have to retrieve your jar again (so probably soon). </li>
<li>In Robot.java, import visualrobot.* and make robot extend VisualRobot. Then, you'll have to implement some methods. Further instructions for that can be found in the sample robot file included in the downloads folder. </li>
</ol>
