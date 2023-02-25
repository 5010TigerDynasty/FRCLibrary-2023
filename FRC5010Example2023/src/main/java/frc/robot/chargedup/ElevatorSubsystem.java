// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chargedup;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxAbsoluteEncoder.Type;
import com.revrobotics.SparkMaxAlternateEncoder;
import com.revrobotics.SparkMaxPIDController;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.ElevatorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.FRC5010.constants.GenericPID;
import frc.robot.FRC5010.motors.MotorController5010;
import frc.robot.FRC5010.motors.hardware.MotorModelConstants;
import frc.robot.FRC5010.sensors.encoder.SimulatedEncoder;

public class ElevatorSubsystem extends SubsystemBase {
  /**
   *
   */
  private static final double pivotOffset = -14.04;
  private final double pivotConversionFactor = 24.242; 
  private MotorController5010 pivotMotor;
  private SparkMaxPIDController pivotController;
  private MotorModelConstants pivotConstants;
  private GenericPID pivotPID;
  private RelativeEncoder pivotEncoder;
  private SimulatedEncoder pivotSimEncoder = new SimulatedEncoder(10, 11);

  private DigitalInput extendHallEffect, pivotHallEffect; 

  private MotorController5010 extendMotor;
  private SparkMaxPIDController extendController;
  private MotorModelConstants extendConstants;
  private GenericPID extendPID;
  private RelativeEncoder extendEncoder;
  private SimulatedEncoder extendSimEncoder = new SimulatedEncoder(12, 13);

  //private double KFF = 0.000156;
  private double kIz = 0;

  private static final double kElevatorDrumRadius = Units.inchesToMeters(2.0);
  private static final double kCarriageMass = 10.0; // kg

  public static final double kMinElevatorHeight = Units.inchesToMeters(24);
  public static final double kMaxElevatorHeight = Units.inchesToMeters(60);

  // distance per pulse = (distance per revolution) / (pulses per revolution)
  //  = (Pi * D) / ppr
  private static final double kElevatorEncoderDistPerPulse = Units.inchesToMeters(14.8828);
      // 2.0 * Math.PI * kElevatorDrumRadius / 8192;

  private Mechanism2d m_mech2d;
  private MechanismRoot2d m_mech2dRoot;
  private MechanismLigament2d m_elevatorMech2d;
  private MechanismLigament2d targetPos2d;

  private double currentPivotTarget;
  private double currentExtendTarget;

  private ElevatorLevel currentLevel = ElevatorLevel.ground; // Unsure of whether this should be stored in subsystem

  // TODO Implement ElevatorFeefForward
  private ElevatorFeedforward extendFeedforward;
  private ElevatorSim extendSim;
  private ArmFeedforward pivotFeedforward;
  private SingleJointedArmSim pivotSim;
  public ElevatorSubsystem(MotorController5010 pivot, GenericPID pivotPID,
      MotorController5010 extend, GenericPID extendPID,
      MotorModelConstants liftConstants, MotorModelConstants extendConstants,
      Mechanism2d mech2d, int extendHallEffectPort, int pivotHallEffectPort) {
    this.currentPivotTarget = 0;
    this.currentExtendTarget = 0;

    this.pivotMotor = pivot;
    this.pivotMotor.setInverted(false);
    this.pivotController = ((CANSparkMax) pivot).getPIDController();
    this.pivotEncoder = ((CANSparkMax) pivot).getAlternateEncoder(SparkMaxAlternateEncoder.Type.kQuadrature, 8192);
    pivotEncoder.setPositionConversionFactor(this.pivotConversionFactor);
    this.pivotEncoder.setInverted(true);
    this.pivotPID = pivotPID;
    this.pivotConstants = liftConstants;


    this.extendMotor = extend;
    this.extendMotor.setInverted(true);
    this.extendController = ((CANSparkMax) extend).getPIDController();
    this.extendEncoder = ((CANSparkMax) extend).getAlternateEncoder(SparkMaxAlternateEncoder.Type.kQuadrature , 8192);
    this.extendEncoder.setPositionConversionFactor(kElevatorEncoderDistPerPulse);
    this.extendPID = extendPID;
    this.extendConstants = extendConstants;

    this.m_mech2d = mech2d;
    m_mech2dRoot = m_mech2d.getRoot("Elevator Root", 5, 20);
    m_elevatorMech2d = m_mech2dRoot.append(
        new MechanismLigament2d(
            "Elevator", Units.metersToInches(kMinElevatorHeight), -30.0, 6, new Color8Bit(Color.kOrange)));
    targetPos2d = m_mech2dRoot.append(
      new MechanismLigament2d("Target", Units.metersToInches(kMinElevatorHeight), -30, 6, new Color8Bit(Color.kBlue)));        
    pivotSim = new SingleJointedArmSim(DCMotor.getNEO(1), 75, 
      40, 2, Units.degreesToRadians(-20), 
      Units.degreesToRadians(60), false);
    extendSim = new ElevatorSim(DCMotor.getNEO(1), 25, 
      kCarriageMass, kElevatorDrumRadius, kMinElevatorHeight, kMaxElevatorHeight, false);

    extendFeedforward = new ElevatorFeedforward(extendConstants.getkS(), extendConstants.getkV(),
        extendConstants.getkA());
    pivotFeedforward = new ArmFeedforward(liftConstants.getkS(), liftConstants.getkF(), liftConstants.getkV());

    pivotController.setP(pivotPID.getkP());
    pivotController.setI(pivotPID.getkI());
    pivotController.setD(pivotPID.getkD());
    pivotController.setFeedbackDevice(pivotEncoder);
    // TODO Set FF and IZ
    //pivotController.setFF(KFF);
    pivotController.setIZone(kIz);
    pivotController.setSmartMotionMaxVelocity(3000, 0);
    pivotController.setSmartMotionMinOutputVelocity(0, 0);
    pivotController.setSmartMotionMaxAccel(100, 0);
      
    extendController.setP(extendPID.getkP());
    extendController.setI(extendPID.getkI());
    extendController.setD(extendPID.getkD());
    extendController.setFeedbackDevice(extendEncoder);

    extendController.setSmartMotionMaxVelocity(3000, 0);
    extendController.setSmartMotionMinOutputVelocity(0, 0);
    extendController.setSmartMotionMaxAccel(100, 0);

    this.extendHallEffect = new DigitalInput(extendHallEffectPort);
    this.pivotHallEffect = new DigitalInput(pivotHallEffectPort);
    
  }

  public void reset() {

  }

  public void setExtendEncoderPosition(double pos) {
    this.extendEncoder.setPosition(pos);
  }

  public void setPivotEncoderPosition(double pos){
    this.pivotEncoder.setPosition(pos);
  }


  public void setPivotPosition(double position) {
    this.currentPivotTarget = position;
    SmartDashboard.putNumber("Pivot Target", currentPivotTarget);
    targetPos2d.setAngle(currentPivotTarget);
    if (Robot.isReal()) {
      //pivotController.setFF(pivotFeedforward.calculate((position * Math.PI) / 180, .25));
      pivotController.setReference(this.currentPivotTarget, CANSparkMax.ControlType.kSmartMotion, 0);
    } else {
      pivotPow((currentPivotTarget - getPivotPosition())/100);
    }
  }

  public void setExtendPosition(double position) {
    this.currentExtendTarget = position;
    SmartDashboard.putNumber("Extend Target", currentExtendTarget);
    targetPos2d.setLength(position);
    if (Robot.isReal()) {
      //extendController.setFF();
      extendController.setReference(this.currentExtendTarget, CANSparkMax.ControlType.kSmartMotion, 0);
    } else {
      extendPow((currentExtendTarget - getExtendPosition()) / kMaxElevatorHeight);
    }
  }

  public double getPivotPosition() {
    if (Robot.isReal()) {
      return pivotEncoder.getPosition() + pivotOffset;
    } else {
      return pivotSimEncoder.getPosition() + pivotOffset;
    }
  }

  public double getExtendPosition() {
    if (Robot.isReal()) {
      return extendEncoder.getPosition() + ElevatorLevel.ground.getExtensionPosition();
    } else {
      return extendSimEncoder.getPosition() + ElevatorLevel.ground.getExtensionPosition();
    }
  }

  public boolean isPivotAtTarget() {
    return Math.abs(getPivotPosition() - this.currentPivotTarget) < 1;
  }

  public boolean isExtendAtTarget() {
    return Math.abs(getExtendPosition() - this.currentExtendTarget) < 0.1;
  }

  public double getPivotTarget() {
    return this.currentPivotTarget;
  }

  public double getExtendTarget() {
    return this.currentExtendTarget;
  }

  public boolean isElevatorIn() {
    return !extendHallEffect.get();
  }

  public boolean isPivotIn(){
    return !pivotHallEffect.get();  
  }



  public ElevatorLevel getElevatorLevel() {
    return this.currentLevel;
  }

  public void setElevatorLevel(ElevatorLevel level) {
    this.currentLevel = level;
    targetPos2d.setLength(currentLevel.getExtensionPosition());
    targetPos2d.setAngle(currentLevel.getPivotPosition());
  }

  public void pivotPow(double pow) {
    SmartDashboard.putNumber("Pivot Power", pow);
    SmartDashboard.putNumber("Pivot Current", ((CANSparkMax) pivotMotor).getOutputCurrent());
    SmartDashboard.putNumber("Pivot Rotation", pivotEncoder.getPosition());
    pivotMotor.set(pow);
  }

  public void extendPow(double pow) {
    extendMotor.set(pow);
    SmartDashboard.putNumber("Elevate Power", extendMotor.get());
    SmartDashboard.putNumber("Elevate Current", ((CANSparkMax) extendMotor).getOutputCurrent());
    SmartDashboard.putNumber("Extend Position", extendEncoder.getPosition());
  }

  public void stopPivot(){
    pivotMotor.set(0);
  }

  public void stopExtend(){
    extendMotor.set(0);
  }

  @Override
  public void periodic() {
    if (Robot.isReal()) {
      m_elevatorMech2d.setLength(Units.metersToInches(getExtendPosition()));
      m_elevatorMech2d.setAngle(getPivotPosition());

      if (isElevatorIn()){
        setExtendEncoderPosition(0);
      }

      if (isPivotIn()){
        setPivotEncoderPosition(0);
      }

      SmartDashboard.putBoolean("Pivot In: ", isPivotIn());
      SmartDashboard.putBoolean("Elevator In: ", isElevatorIn());

    }
    SmartDashboard.putNumber("Motor Pow: ", extendMotor.get());  
    SmartDashboard.putNumber("Pivot Pow: ", pivotMotor.get());
    
    SmartDashboard.putNumber("Elevator Position: ", getExtendPosition());
    SmartDashboard.putNumber("Pivot Position: ", getPivotPosition());
    //SmartDashboard.putNumber("Abs", KFF);
    SmartDashboard.putBoolean("Is Elevator in: ", isElevatorIn());
  }

  @Override
  public void simulationPeriodic() {
    // In this method, we update our simulation of what our elevator is doing
    // First, we set our "inputs" (voltages)
    extendSim.setInput(extendMotor.get() * RobotController.getBatteryVoltage());
    pivotSim.setInput(pivotMotor.get() * RobotController.getBatteryVoltage());

    // Next, we update it. The standard loop time is 20ms.
    extendSim.update(0.020);
    pivotSim.update(0.020);

    // Finally, we set our simulated encoder's readings and simulated battery
    // voltage
    extendSimEncoder.setPosition(extendSim.getPositionMeters());
    pivotSimEncoder.setPosition(Units.radiansToDegrees(pivotSim.getAngleRads()));
    // SimBattery estimates loaded battery voltages
    RoboRioSim.setVInVoltage(
        BatterySim.calculateDefaultBatteryLoadedVoltage(extendSim.getCurrentDrawAmps()));
    RoboRioSim.setVInVoltage(
        BatterySim.calculateDefaultBatteryLoadedVoltage(pivotSim.getCurrentDrawAmps()));

    // Update elevator visualization with simulated position
    SmartDashboard.putNumber("Pivot Sim Rotation", Units.radiansToDegrees(pivotSim.getAngleRads()));
    m_elevatorMech2d.setLength(Units.metersToInches(extendSim.getPositionMeters()));
    m_elevatorMech2d.setAngle(Units.radiansToDegrees(pivotSim.getAngleRads()));
  }
}