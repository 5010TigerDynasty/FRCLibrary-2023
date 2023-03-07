// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import com.revrobotics.RelativeEncoder;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.chargedup.ElevatorSubsystem;

public class HomeElevator extends CommandBase {
  ElevatorSubsystem elevatorSubsystem;

  /** Creates a new HomeElevator. */
  public HomeElevator(ElevatorSubsystem elevatorSubsystem) {
    this.elevatorSubsystem = elevatorSubsystem;
    addRequirements(elevatorSubsystem);
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {

  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    this.elevatorSubsystem.runExtendToTarget(0);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    this.elevatorSubsystem.stopAndHoldExtend();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return this.elevatorSubsystem.isElevatorIn();
  }
}
