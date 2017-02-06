# BuildAnAuton_2.0 (NOTE: INSTRUCTIONS HAVE CHANGED FOR 2017)
The necessary jars can be found in the downloads folder.

Instructions for setup:
<ol>
<li>Put the jar in the same folder as the WPILib jars. This is usually found in [User]/wpilib/user/java/lib. </li>
<li>Make your robot project. </li>
<li>Add the jar to your robot projects build path. For eclipse, go to Properties -> Java Build Path -> Add External Jar and select the downloaded jar. </li>
<li>Go into build.properties and in the userLibs property put in the path of the jar that you just moved. The entire line should be userLibs=[User]/wpilib/user/java/lib/. </li>
<li>In Robot.java, import visualrobot.* and make robot extend VisualRobot. Then, you'll have to implement some methods. Further instructions for that can be found in the sample robot file included in the downloads folder. </li>
</ol>
