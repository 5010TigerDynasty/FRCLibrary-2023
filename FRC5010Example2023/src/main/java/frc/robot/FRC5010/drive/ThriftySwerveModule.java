// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.FRC5010.drive;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import frc.robot.FRC5010.constants.GenericMotorConstants;
import frc.robot.FRC5010.constants.GenericPID;
import frc.robot.FRC5010.constants.GenericSwerveModuleConstants;
import frc.robot.FRC5010.constants.SwervePorts;
import frc.robot.FRC5010.motors.hardware.NEO;
import frc.robot.FRC5010.sensors.AnalogInput5010;

/** Add your docs here. */
public class ThriftySwerveModule extends GenericSwerveModule{

    private GenericPID pid = new GenericPID(0.052037 * 4, 0, 0); 
    private GenericMotorConstants motorConstants = new GenericMotorConstants(0.55641, 0.064889, 0.0025381);
    private GenericSwerveModuleConstants moduleConstants = new GenericSwerveModuleConstants(Units.inchesToMeters(3), 1/5.25, true, 1/((5.33) * 10.5), true); 
    
    
    public ThriftySwerveModule(MechanismRoot2d visualRoot, String key, double radOffset, SwervePorts swervePorts) {
        super(visualRoot, key, radOffset);
        super.pid = this.pid;
        super.motorConstants = this.motorConstants;
        super.moduleConstants = this.moduleConstants;
        drive = new NEO(swervePorts.getDrivePort()).invert(moduleConstants.isDrivingInv());
        turn = new NEO(swervePorts.getTurnPort()).invert(moduleConstants.isTurningInv());
        absoluteEncoder = new AnalogInput5010(swervePorts.getEncoderPort());
        turnEncoder = turn.getMotorEncoder();
        driveEncoder = drive.getMotorEncoder();

        // set units drive encoder to meters and meters/sec
        driveEncoder.setPositionConversion(moduleConstants.getkDriveEncoderRot2Meter());
        driveEncoder.setVelocityConversion(moduleConstants.getkDriveEncoderRPM2MeterPerSec());
        // set units turning encoder to radians and radians/sec
        turnEncoder.setPositionConversion(moduleConstants.getkTurningEncoderRot2Rad());
        turnEncoder.setVelocityConversion(moduleConstants.getkTurningEncoderRPM2RadPerSec());

    }    

}
