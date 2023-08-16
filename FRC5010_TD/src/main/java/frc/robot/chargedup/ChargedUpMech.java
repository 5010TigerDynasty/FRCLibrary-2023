// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chargedup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pathplanner.lib.PathPlannerTrajectory;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.FRC5010.constants.GenericPID;
import frc.robot.FRC5010.mechanisms.GenericMechanism;
import frc.robot.FRC5010.motors.MotorFactory;
import frc.robot.FRC5010.motors.hardware.MotorModelConstants;
import frc.robot.FRC5010.sensors.ButtonBoard;
import frc.robot.FRC5010.sensors.Controller;
import frc.robot.FRC5010.subsystems.LedSubsystem;
import frc.robot.commands.HomePivot;
import frc.robot.commands.IntakeSpin;
import frc.robot.commands.LedDefaultCommand;
import frc.robot.commands.PivotArm;

/** Add your docs here. */
public class ChargedUpMech extends GenericMechanism {
        private IntakeSubsystem intakeSubsystem;
        private PivotSubsystem pivotSubsystem;
        private ButtonBoard buttonOperator;
        private final double kArmMaxSpeedLimit = 1;
        private final double kArmMinSpeedLimit = 0.25;
        private final double kSpinMaxSpeed = 0.6;
        private final double kSpinLowSpeed = 0.35;
        private double speedLimit = kArmMaxSpeedLimit;
        private double intakeSpeedLimit = kSpinMaxSpeed;
        private boolean ledConePickUp = false;
        private LedSubsystem ledSubsystem;

        private ArmLevel armLevel = ArmLevel.ground;

        public ChargedUpMech(Mechanism2d robotMechVisual, ShuffleboardTab shuffleTab, ButtonBoard buttonOperator,
                        LedSubsystem ledSubsystem) {
                super(robotMechVisual, shuffleTab);
                // use this to PID the Elevator
                // https://www.chiefdelphi.com/t/is-tuning-spark-max-smart-motion-impossible/404104/2
                this.pivotSubsystem = new PivotSubsystem(
                                MotorFactory.NEO(9),
                                new GenericPID(12, 0.0, 0.03),
                                new MotorModelConstants(0.25, 2.125, 0),
                                1, 8,
                                mechVisual);

                this.intakeSubsystem = new IntakeSubsystem(
                                MotorFactory.NEO(19),
                                MotorFactory.NEO(18),
                                new MotorModelConstants(0, 0, 0),
                                new GenericPID(0.003, 0, 0),
                                new DoubleSolenoid(PneumaticsModuleType.REVPH, 0, 1),
                                mechVisual);
                // TODO: Set up IntakeSubsystem add correct values please
                this.buttonOperator = buttonOperator;
                this.ledSubsystem = ledSubsystem;
        }

        @Override
        public void configureButtonBindings(Controller driver, Controller operator) {

                // Move to current extend level
                buttonOperator.getButton(6)
                                .onTrue(
                                                new SequentialCommandGroup(
                                                                new InstantCommand(() -> {
                                                                        armLevel = ArmLevel.ground;
                                                                }),
                                                                new PivotArm(pivotSubsystem, ArmLevel.ground)

                                                ));

                operator.createAButton().onTrue(
                                new SequentialCommandGroup(
                                                new InstantCommand(() -> {
                                                        armLevel = ArmLevel.ground;
                                                }),
                                                new PivotArm(pivotSubsystem, ArmLevel.ground)

                                ));

                buttonOperator.getButton(5)
                                .onTrue(
                                                new SequentialCommandGroup(
                                                                new InstantCommand(
                                                                                () -> armLevel = ArmLevel.loading),
                                                                new PivotArm(pivotSubsystem, ArmLevel.loading)

                                                ));

                operator.createXButton().onTrue(
                                new SequentialCommandGroup(
                                                new InstantCommand(
                                                                () -> armLevel = ArmLevel.loading),
                                                new PivotArm(pivotSubsystem, ArmLevel.loading)

                                ));

                buttonOperator.getButton(4)
                                .onTrue(
                                                new SequentialCommandGroup(
                                                                new InstantCommand(
                                                                                () -> armLevel = ArmLevel.medium),
                                                                new PivotArm(pivotSubsystem, ArmLevel.medium)

                                                ));
                operator.createYButton().onTrue(
                                new SequentialCommandGroup(
                                                new InstantCommand(
                                                                () -> armLevel = ArmLevel.medium),
                                                new PivotArm(pivotSubsystem, ArmLevel.medium)

                                ));

                buttonOperator.getButton(3)
                                .onTrue(
                                                new SequentialCommandGroup(
                                                                new InstantCommand(
                                                                                () -> armLevel = ArmLevel.high),
                                                                new PivotArm(pivotSubsystem, ArmLevel.high)));

                operator.createBButton().onTrue(
                                new SequentialCommandGroup(
                                                new InstantCommand(
                                                                () -> armLevel = ArmLevel.high),
                                                new PivotArm(pivotSubsystem, ArmLevel.high)));

                buttonOperator.getButton(7)
                                .onTrue(new InstantCommand(() -> {
                                        speedLimit = kArmMinSpeedLimit;
                                        intakeSpeedLimit = kSpinLowSpeed;
                                }))
                                .onFalse(new InstantCommand(() -> {
                                        intakeSpeedLimit = kSpinMaxSpeed;
                                        speedLimit = kArmMaxSpeedLimit;
                                }));

                buttonOperator.getButton(8).onTrue(new InstantCommand(() -> {
                        intakeSubsystem.setIntakeCone();
                }, intakeSubsystem));

                operator.createLeftBumper().onTrue(new InstantCommand(() -> {
                        intakeSubsystem.setIntakeCone();
                }, intakeSubsystem));

                buttonOperator.getButton(9).onTrue(new InstantCommand(() -> {
                        intakeSubsystem.setIntakeCube();
                }, intakeSubsystem));

                operator.createRightBumper().onTrue(new InstantCommand(() -> {
                        intakeSubsystem.setIntakeCube();
                }, intakeSubsystem));

                buttonOperator.getButton(10)
                                .whileTrue(new IntakeSpin(intakeSubsystem, () -> Math.max(-intakeSpeedLimit, -1)));

                buttonOperator.setYAxis(buttonOperator.createYAxis().negate().deadzone(0.05));
                buttonOperator.setXAxis(buttonOperator.createXAxis().deadzone(0.05)); // The deadzone isnt technically

                driver.setRightTrigger(driver.createRightTrigger());
                driver.setLeftTrigger(driver.createLeftTrigger());

                driver.createUpPovButton().whileTrue(new HomePivot(pivotSubsystem));

                operator.setRightTrigger(operator.createRightTrigger());
                operator.setLeftTrigger(operator.createLeftTrigger());
                // necessary
                // but I have seen self movement
                // without
                // it

                // new Trigger(() -> (Math.abs(buttonOperator.getXAxis()) > 0.01))
                // .onTrue(new ElevatorPower(elevatorSubsystem, () -> (buttonOperator.getXAxis()
                // * speedLimit))
                // );

                // new Trigger(() -> (Math.abs(buttonOperator.getYAxis()) > 0.01))
                // .onTrue(new PivotPower(pivotSubsystem, () -> (buttonOperator.getYAxis() *
                // speedLimit))
                // );

                new Trigger(() -> (Math
                                .abs(driver.getRightTrigger() - driver.getLeftTrigger()) > 0.01))
                                .whileTrue(new IntakeSpin(intakeSubsystem,
                                                () -> (driver.getRightTrigger()
                                                                - driver.getLeftTrigger()) * 1));

                new Trigger(() -> (Math
                                .abs(operator.getRightTrigger()) > 0.01))
                                .whileTrue(new IntakeSpin(intakeSubsystem,
                                                () -> (operator.getRightTrigger() * -1)));

                driver.createDownPovButton()
                                .onTrue(new InstantCommand(() -> pivotSubsystem.toggleOverride(), pivotSubsystem));

                driver.createXButton().onTrue(new InstantCommand(() -> {
                        pivotSubsystem.toggleOverride();
                }, pivotSubsystem));

                operator.setRightYAxis(operator.createRightYAxis().deadzone(0.2).negate());
                operator.setLeftYAxis(operator.createLeftYAxis().deadzone(0.2));

                driver.createRightBumper().onTrue(new InstantCommand(() -> ledSubsystem.togglePickUp(), ledSubsystem));
        }

        public IntakeSubsystem getIntakeSubsystem() {
                return intakeSubsystem;
        }

        public PivotSubsystem getPivotSubsystem() {
                return pivotSubsystem;
        }

        @Override
        public void setupDefaultCommands(Controller driver, Controller operator) {
                pivotSubsystem.setDefaultCommand(new FunctionalCommand(
                                () -> {
                                },
                                () -> {
                                        pivotSubsystem.pivotPow(buttonOperator.getYAxis() * speedLimit
                                                        + operator.getRightYAxis(), true);

                                },
                                (Boolean interrupted) -> {
                                        pivotSubsystem.pivotPow(0, true);
                                },
                                () -> false,
                                pivotSubsystem));
                ledSubsystem.setDefaultCommand(new LedDefaultCommand(ledSubsystem, intakeSubsystem));
        }

        @Override
        public void disabledBehavior() {
                ledSubsystem.setRainbow();
        }

        @Override
        protected void initRealOrSim() {
        }

        @Override
        public Map<String, List<PathPlannerTrajectory>> initAutoCommands() {
                return new HashMap<>();
        }

        @Override
        public Command generateAutoCommand(List<PathPlannerTrajectory> paths) {
                // TODO Auto-generated method stub
                return null;
        }
}
