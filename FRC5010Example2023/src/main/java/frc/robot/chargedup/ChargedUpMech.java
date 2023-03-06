// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chargedup;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.FRC5010.constants.GenericPID;
import frc.robot.FRC5010.drive.GenericDrivetrain;
import frc.robot.FRC5010.drive.swerve.SwerveDrivetrain;
import frc.robot.FRC5010.mechanisms.GenericMechanism;
import frc.robot.FRC5010.motors.MotorFactory;
import frc.robot.FRC5010.motors.hardware.MotorModelConstants;
import frc.robot.FRC5010.sensors.ButtonBoard;
import frc.robot.FRC5010.sensors.Controller;
import frc.robot.FRC5010.subsystems.LedSubsystem;
import frc.robot.commands.PivotPower;
import frc.robot.commands.ElevatorPower;
import frc.robot.commands.HomeElevator;
import frc.robot.commands.HomePivot;
import frc.robot.commands.IntakeSpin;
import frc.robot.commands.LedDefaultCommand;
import frc.robot.commands.MoveElevator;
import frc.robot.commands.PivotElevator;

/** Add your docs here. */
public class ChargedUpMech extends GenericMechanism {
    private ElevatorSubsystem elevatorSubsystem;
    private IntakeSubsystem intakeSubsystem;
    private PivotSubsystem pivotSubsystem;
    private ButtonBoard buttonOperator;
    private double speedLimit = .5;
    private boolean ledConePickUp = false;
    private LedSubsystem ledSubsystem;

    private ElevatorLevel elevatorLevel = ElevatorLevel.ground;

    public ChargedUpMech(Mechanism2d robotMechVisual, ShuffleboardTab shuffleTab, ButtonBoard buttonOperator, LedSubsystem ledSubsystem) {
        super(robotMechVisual, shuffleTab);
        // use this to PID the Elevator
        // https://www.chiefdelphi.com/t/is-tuning-spark-max-smart-motion-impossible/404104/2
        this.elevatorSubsystem = new ElevatorSubsystem(
                MotorFactory.NEO(11), new GenericPID(10, 0, 0),
                new MotorModelConstants(1, 1, 1),
                mechVisual, 0, () -> pivotSubsystem.getPivotPosition());

        this.pivotSubsystem = new PivotSubsystem(
            MotorFactory.NEO(9), 
            new GenericPID(2, 0.0, 0.03), 
            new MotorModelConstants(1, 1, 1), 
            1, 8, 
            () -> elevatorSubsystem.getExtendPosition(), mechVisual);

        this.intakeSubsystem = new IntakeSubsystem(
                MotorFactory.NEO(19), 
                MotorFactory.NEO(18), 
                new MotorModelConstants(0, 0, 0), 
                new GenericPID(0.003, 0, 0), 
                new DoubleSolenoid(PneumaticsModuleType.REVPH, 0, 1), 
                robotMechVisual
        );
        // TODO: Set up IntakeSubsystem add correct values please
        this.buttonOperator = buttonOperator;
        this.ledSubsystem = ledSubsystem;    
    }

    @Override
    public void configureButtonBindings(Controller driver, Controller operator) {
        
        buttonOperator.getButton(1)
                .whileTrue(new MoveElevator(elevatorSubsystem, () -> elevatorLevel));

        buttonOperator.getButton(2)
                .whileTrue(new HomeElevator(elevatorSubsystem));

        buttonOperator.getButton(6)
                .onTrue(
                    new SequentialCommandGroup(
                        new InstantCommand(() -> {elevatorLevel = ElevatorLevel.ground;}),
                        new PivotElevator(pivotSubsystem, ElevatorLevel.ground)
                        
                    )
                );

        buttonOperator.getButton(5)
                .onTrue(
                    new SequentialCommandGroup(
                        new InstantCommand( () -> elevatorLevel = ElevatorLevel.low),
                        new PivotElevator(pivotSubsystem, ElevatorLevel.low)
                        
                    )    
                );

        buttonOperator.getButton(4)
                .onTrue(
                    new SequentialCommandGroup(
                        new InstantCommand( () -> elevatorLevel = ElevatorLevel.medium),
                        new PivotElevator(pivotSubsystem, ElevatorLevel.medium)
                        
                    )
                );

        buttonOperator.getButton(3)
                .onTrue(
                    new SequentialCommandGroup(
                        new InstantCommand( () -> elevatorLevel = ElevatorLevel.high),
                        new PivotElevator(pivotSubsystem, ElevatorLevel.high)
                    )
                );

        buttonOperator.getButton(7)
                .onTrue(new InstantCommand(() -> {speedLimit = 0.25;}))
                .onFalse(new InstantCommand(() -> {speedLimit = 0.5;}));

        buttonOperator.getButton(8).onTrue(new InstantCommand(() -> {intakeSubsystem.setIntakeCone();}, intakeSubsystem));

        buttonOperator.getButton(9).onTrue(new InstantCommand(() -> {intakeSubsystem.setIntakeCube();}, intakeSubsystem));

        buttonOperator.getButton(10).whileTrue(new IntakeSpin(intakeSubsystem, () -> Math.max(-speedLimit * 1.2, -1)));

        buttonOperator.setYAxis(buttonOperator.createYAxis().negate().deadzone(0.05));
        buttonOperator.setXAxis(buttonOperator.createXAxis().deadzone(0.05)); //The deadzone isnt technically necessary but I have seen self movement without it

        
        // new Trigger(() -> (Math.abs(buttonOperator.getXAxis()) > 0.01))
        //     .onTrue(new ElevatorPower(elevatorSubsystem, () -> (buttonOperator.getXAxis() * speedLimit))
        // );

        // new Trigger(() -> (Math.abs(buttonOperator.getYAxis()) > 0.01))
        //     .onTrue(new PivotPower(pivotSubsystem, () -> (buttonOperator.getYAxis() * speedLimit))
        // );

        new Trigger(() -> (Math.abs(driver.createRightTrigger().get() - driver.createLeftTrigger().get()) > 0.01))
                .whileTrue(new IntakeSpin(intakeSubsystem, () -> (driver.createRightTrigger().get() - driver.createLeftTrigger().get())* .7));
        
        driver.createStartButton().onTrue(new InstantCommand(() -> pivotSubsystem.toggleOverride(), pivotSubsystem));

        operator.createBButton().whileTrue(new HomePivot(pivotSubsystem));
        operator.setRightYAxis(operator.createRightYAxis().deadzone(.2).negate());
        operator.setLeftYAxis(operator.createLeftYAxis().deadzone(0.2));

        driver.createRightBumper().onTrue(new InstantCommand(() -> ledSubsystem.togglePickUp(), ledSubsystem));
    }

    public IntakeSubsystem getIntakeSubsystem(){
        return intakeSubsystem;
    }

    public ElevatorSubsystem getElevatorSubsystem(){
        return elevatorSubsystem;
    }

    public PivotSubsystem getPivotSubsystem(){
        return pivotSubsystem;
    }
    
    @Override
    public void setupDefaultCommands(Controller driver, Controller operator) {
        elevatorSubsystem.setDefaultCommand(new FunctionalCommand(
                () -> {
                },
                () -> {
                    pivotSubsystem.pivotPow(buttonOperator.getYAxis() * speedLimit + operator.getRightYAxis(),true);
                    elevatorSubsystem.extendPow(buttonOperator.getXAxis() * speedLimit + operator.getLeftYAxis());
                },
                (Boolean interrupted) -> {
                    pivotSubsystem.pivotPow(0,true);
                    elevatorSubsystem.extendPow(0);
                },
                () -> false,
                elevatorSubsystem));

        ledSubsystem.setDefaultCommand(new LedDefaultCommand(ledSubsystem, intakeSubsystem));
    }

    @Override
    protected void initRealOrSim() {
    }

    @Override
    public Map<String, Command> initAutoCommands() {
        return new HashMap<>();
    }
}
