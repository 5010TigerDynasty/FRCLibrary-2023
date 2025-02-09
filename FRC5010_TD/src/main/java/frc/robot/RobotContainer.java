// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pathplanner.lib.PathPlannerTrajectory;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.FRC5010.constants.GenericMechanism;
import frc.robot.FRC5010.constants.Persisted;
import frc.robot.FRC5010.constants.PersistedEnums;
import frc.robot.FRC5010.constants.RobotConstantsDef;
import frc.robot.FRC5010.robots.BabySwerve;
import frc.robot.FRC5010.robots.CurtsLaptopSimulator;
import frc.robot.FRC5010.robots.DefaultRobot;
import frc.robot.FRC5010.robots.PracticeBot;
import frc.robot.FRC5010.sensors.Controller;
import frc.robot.FRC5010.telemetery.WpiDataLogging;
import frc.robot.chargedup.CompBot_2023_T1G3R;
import frc.robot.chargedup.CubeCruzer;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer extends GenericMechanism {
  // The robot's subsystems and commands are defined here...
  private SendableChooser<List<PathPlannerTrajectory>> command = new SendableChooser<>();
  private Controller driver;
  private Controller operator;
  private static Alliance alliance;
  public static Constants constants;
  private GenericMechanism robot;
  private static String MAC_Address = "MAC ADDRESS";

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    super("Robot");
    // Create a Mechanism2d display for simulating robot functions
    constants = new Constants();
    values.declare(MAC_Address, "");

    // Setup controllers
    driver = new Controller(Controller.JoystickPorts.ZERO.ordinal());
    operator = new Controller(Controller.JoystickPorts.ONE.ordinal());
    if (!operator.isPluggedIn()) {
      operator = driver;
      driver.setSingleControllerMode(true);
    }

    // Put Mechanism 2d to SmartDashboard
    mechVisual = new Mechanism2d(PersistedEnums.ROBOT_VISUAL_H.getInteger(),
        RobotConstantsDef.robotVisualV.getInteger());
    SmartDashboard.putData("Robot Visual", mechVisual);

    DriverStation.silenceJoystickConnectionWarning(true);
    alliance = determineAllianceColor();
    initRealOrSim();

    // Configure the button bindings
    configureButtonBindings(driver, operator);
    initAutoCommands();
    SmartDashboard.putData(logPrefix, this);
  }

  public static String WHO_AM_I = "WhoAmI";
  private static Persisted<String> whoAmI = new Persisted<>(WHO_AM_I, "Simulator");

  // Robot types
  public static class Robots {
    public static final String CC_BOT_2023 = "00:80:2F:33:17:DD";
    public static final String COMP_BOT_2023 = "00:80:2F:33:04:33";
    public static final String BABY_SWERVE = "BabySwerve";
    public static final String PRACTICE_BOT = "PracticeBot";
    public static final String CURTS_LAPTOP_SIM = "D2:57:7B:3E:C0:47";
  }

  /**
   * For things being initialized in RobotContainer, provide a simulation version
   */
  protected void initRealOrSim() {
    if (RobotBase.isReal()) {
      WpiDataLogging.start(false);
    } else {
      WpiDataLogging.start(false);
      // NetworkTableInstance instance = NetworkTableInstance.getDefault();
      // instance.stopServer();
      // set the NT server if simulating this code.
      // "localhost" for photon on desktop, or "photonvision.local" / "[ip-address]"
      // for coprocessor
      // instance.setServer("localhost");
      // instance.startClient4("myRobot");
    }

    robotFactory();
  }

  private String whereAmI() {
    try {
      NetworkInterface myNI = NetworkInterface.networkInterfaces().filter(it -> {
        try {
          byte[] MA = it.getHardwareAddress();
          return null != MA;
        } catch (Exception e) {
          e.printStackTrace();
        }
        return false;
      }).findFirst().orElse(NetworkInterface.networkInterfaces().findFirst().get());
      byte[] MAC_ADDRESS = myNI.getHardwareAddress();
      final List<Byte> macList = new ArrayList<>();
      if (null != MAC_ADDRESS) {
        for (byte b : MAC_ADDRESS) {
          macList.add(b);
        }
      }
      String whichRobot = macList.stream().map(it -> String.format("%02X", it))
          .collect(Collectors.joining(":"));
      values.set(MAC_Address, whichRobot.toString());
      return whichRobot.toString();
    } catch (SocketException e) {
      e.printStackTrace();
    }
    return "unknown";
  }

  private void robotFactory() {
    String whichRobot = whereAmI();
    switch (whichRobot) {
      case Robots.CC_BOT_2023: {
        robot = new CubeCruzer(mechVisual, shuffleTab);
        break;
      }
      case Robots.COMP_BOT_2023: {
        robot = new CompBot_2023_T1G3R(mechVisual, shuffleTab);
        break;
      }
      case Robots.BABY_SWERVE: {
        robot = new BabySwerve(mechVisual, shuffleTab);
        break;
      }
      case Robots.PRACTICE_BOT: {
        robot = new PracticeBot(mechVisual, shuffleTab);
        break;
      }
      case Robots.CURTS_LAPTOP_SIM: {
        switch (whoAmI.get()) {
          case "BabySwerve": {
            robot = new BabySwerve(mechVisual, shuffleTab);
            break;
          }
          case "T1G3R": {
            robot = new CompBot_2023_T1G3R(mechVisual, shuffleTab);
            break;
          }
          case "Simulator": {
            robot = new CurtsLaptopSimulator(mechVisual, shuffleTab);
            break;
          }
        }
        break;
      }
      default: {
        robot = new DefaultRobot(mechVisual, shuffleTab);
        break;
      }
    }
    log(">>>>>>>>>> Running " + robot.getClass().getSimpleName() + " <<<<<<<<<<");
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing
   * it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  public void configureButtonBindings(Controller driver, Controller operator) {
    robot.configureButtonBindings(driver, operator);
    if (driver.isSingleControllerMode()) {
      // TODO: Add code to handle single driver mode
    } else {
      if (RobotBase.isReal()) {
      }
    }
  }

  @Override
  public void setupDefaultCommands(Controller driver, Controller operator) {
    if (!DriverStation.isTest()) {
      robot.setupDefaultCommands(driver, operator);
    } else {
      /**
       * TODO: Test mode default commands
       */
    }
  }

  // Just sets up defalt commands (setUpDeftCom)
  public void setupDefaultCommands() {
    setupDefaultCommands(driver, operator);
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {

    return generateAutoCommand(command.getSelected());
  }

  @Override
  public Map<String, List<PathPlannerTrajectory>> initAutoCommands() {
    Map<String, List<PathPlannerTrajectory>> autoCommands = robot.initAutoCommands();
    command.setDefaultOption("Do nothing auto", new ArrayList<>());
    if (null != autoCommands) {
      for (String name : autoCommands.keySet()) {
        command.addOption(name, autoCommands.get(name));
      }
      shuffleTab.add("Auto Modes", command).withSize(2, 1);
    }
    return autoCommands;
  }

  public Alliance determineAllianceColor() {
    Alliance color = DriverStation.getAlliance();
    if (Alliance.Red.equals(color)) {
      /**
       * TODO: What to setup if alliance is Red
       */
    } else if (Alliance.Blue.equals(color)) {
      /**
       * TODO: What to setup if alliance is Blue
       */
    } else {
      /**
       * TODO: What to setup if alliance is not set?
       */
    }
    // Return alliance color so that setup functions can also store/pass
    return color;
  }

  public static Alliance getAlliance() {
    return alliance;
  }

  public void disabledBehavior() {
    robot.disabledBehavior();
  }

  @Override
  public Command generateAutoCommand(List<PathPlannerTrajectory> paths) {
    return robot.generateAutoCommand(command.getSelected());
  }
}
