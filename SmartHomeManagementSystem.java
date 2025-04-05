/**
 * Input libraries which we need.
 */
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Scanner;

/**
 * The Main class serves as the entry point for the Smart Home Management System.
 */
public final class SmartHomeManagementSystem {
    /**
     * Constants defining device properties.
     */
    private static final int MIN_DEVICE_ID = 0;
    private static final int MAX_DEVICE_ID = 9;
    private static final int LIGHT_DEVICE_COUNT = 4;
    private static final int CAMERA_DEVICE_COUNT = 2;
    private static final int CAMERA_DEFAULT_ANGLE = 45;
    private static final int HEATER_DEFAULT_TEMPERATURE = 20;
    private static final int COMMAND_PARTS_DEVICE_ID_INDEX = 2;
    private static final int COMMAND_PARTS_VALUE_INDEX = 3;
    private static final int ARG_COUNT_TURN_ON_OFF = 3;
    private static final int ARG_COUNT_SET_BRIGHTNESS_COLOR = 4;
    private static final int ARG_COUNT_START_STOP_CHARGING = 3;
    private static final int ARG_COUNT_SET_TEMPERATURE_ANGLE = 4;
    private static final int ARG_COUNT_START_STOP_RECORDING = 3;
    private static final int ARG_COUNT_DISPLAY_ALL_STATUS = 1;

    /**
     * Data structures for devices, logs, and valid commands.
     */
    private static final Map<Integer, SmartDevice> DEVICES = new HashMap<>();
    private static final List<String> OUTPUT_LOG = new ArrayList<>();
    private static final Set<String> VALID_COMMANDS = new HashSet<>(Arrays.asList(
            "TurnOn", "TurnOff", "SetBrightness", "SetColor", "StartCharging", "StopCharging",
            "DisplayAllStatus", "SetTemperature", "SetAngle", "StartRecording", "StopRecording"
    ));
    private static final Set<String> VALID_DEVICE_TYPES = new HashSet<>(Arrays.asList(
            "Light", "Camera", "Heater"
    ));
    private static final Map<String, Integer> COMMAND_ARGUMENT_COUNT = new HashMap<>();

    /**
     * Static initializer block to set up the system.
     * Initializes command argument expectations and populates the list of devices.
     */
    static {
        /**
         * Set up command argument counts.
         */
        COMMAND_ARGUMENT_COUNT.put("TurnOn", ARG_COUNT_TURN_ON_OFF);
        COMMAND_ARGUMENT_COUNT.put("TurnOff", ARG_COUNT_TURN_ON_OFF);
        COMMAND_ARGUMENT_COUNT.put("SetBrightness", ARG_COUNT_SET_BRIGHTNESS_COLOR);
        COMMAND_ARGUMENT_COUNT.put("SetColor", ARG_COUNT_SET_BRIGHTNESS_COLOR);
        COMMAND_ARGUMENT_COUNT.put("StartCharging", ARG_COUNT_START_STOP_CHARGING);
        COMMAND_ARGUMENT_COUNT.put("StopCharging", ARG_COUNT_START_STOP_CHARGING);
        COMMAND_ARGUMENT_COUNT.put("SetTemperature", ARG_COUNT_SET_TEMPERATURE_ANGLE);
        COMMAND_ARGUMENT_COUNT.put("SetAngle", ARG_COUNT_SET_TEMPERATURE_ANGLE);
        COMMAND_ARGUMENT_COUNT.put("StartRecording", ARG_COUNT_START_STOP_RECORDING);
        COMMAND_ARGUMENT_COUNT.put("StopRecording", ARG_COUNT_START_STOP_RECORDING);
        COMMAND_ARGUMENT_COUNT.put("DisplayAllStatus", ARG_COUNT_DISPLAY_ALL_STATUS);

        /**
         * Initialize devices.
         */
        for (int i = MIN_DEVICE_ID; i <= MAX_DEVICE_ID; i++) {
            if (i < LIGHT_DEVICE_COUNT) {
                DEVICES.put(i, new Light(Status.ON, i, LightColor.YELLOW, BrightnessLevel.LOW, false));
            } else if (i < LIGHT_DEVICE_COUNT + CAMERA_DEVICE_COUNT) {
                DEVICES.put(i, new Camera(Status.ON, i, false, false, CAMERA_DEFAULT_ANGLE, false));
            } else {
                DEVICES.put(i, new Heater(Status.ON, i, HEATER_DEFAULT_TEMPERATURE));
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String command;
        while (true) {
            command = scanner.nextLine();
            if (command.equalsIgnoreCase("end")) {
                break;
            }
            processCommand(command);
        }
        scanner.close();
        displayOutputLog();
    }

    /**
     * Processes a single user command, validates its structure, and delegates
     * it to the appropriate method for execution.
     * @param command The user command as a string.
     */
    private static void processCommand(String command) {
        if (command.trim().isEmpty()) {
            OUTPUT_LOG.add("Invalid command");
            return;
        }

        String[] parts = command.trim().split("\\s+");

        if (parts.length == 1 && parts[0].equalsIgnoreCase("DisplayAllStatus")) {
            displayAllStatus();
            return;
        }

        String action = parts[0];
        if (!VALID_COMMANDS.contains(action)) {
            OUTPUT_LOG.add("Invalid command");
            return;
        }

        int expectedArgs = COMMAND_ARGUMENT_COUNT.getOrDefault(action, -1);
        if (expectedArgs == -1 || parts.length != expectedArgs) {
            OUTPUT_LOG.add("Invalid command");
            return;
        }

        int deviceId;
        try {
            deviceId = Integer.parseInt(parts[COMMAND_PARTS_DEVICE_ID_INDEX]);
        } catch (NumberFormatException e) {
            OUTPUT_LOG.add("Invalid command");
            return;
        }

        SmartDevice device = DEVICES.get(deviceId);
        if (device == null) {
            OUTPUT_LOG.add("The smart device was not found");
            return;
        }

        String deviceName = parts[1];
        if (!VALID_DEVICE_TYPES.contains(deviceName)) {
            OUTPUT_LOG.add("The smart device was not found");
            return;
        }

        if (!device.getClass().getSimpleName().equalsIgnoreCase(deviceName)) {
            OUTPUT_LOG.add("The smart device was not found");
            return;
        }

        if (!action.equalsIgnoreCase("StartCharging")
                && !action.equalsIgnoreCase("StopCharging")
                && !action.equalsIgnoreCase("TurnOn")
                && !action.equalsIgnoreCase("TurnOff")) {

            if (!device.isOn()) {
                OUTPUT_LOG.add("You can't change the status of the " + deviceName + " "
                        + deviceId + " while it is off");
                return;
            }
        }

        if (action.equalsIgnoreCase("StartCharging") || action.equalsIgnoreCase("StopCharging")) {
            if (!(device instanceof Chargeable)) {
                OUTPUT_LOG.add(deviceName + " " + deviceId + " is not chargeable");
                return;
            }
        }

        switch (action) {
            case "TurnOn":
                turnOnDevice(deviceName, deviceId);
                break;
            case "TurnOff":
                turnOffDevice(deviceName, deviceId);
                break;
            case "SetBrightness":
                setBrightness(deviceName, deviceId, parts[COMMAND_PARTS_VALUE_INDEX]);
                break;
            case "SetColor":
                setColor(deviceName, deviceId, parts[COMMAND_PARTS_VALUE_INDEX]);
                break;
            case "StartCharging":
                startChargingDevice(deviceName, deviceId);
                break;
            case "StopCharging":
                stopChargingDevice(deviceName, deviceId);
                break;
            case "SetTemperature":
                try {
                    int temperature = Integer.parseInt(parts[COMMAND_PARTS_VALUE_INDEX]);
                    setTemperature(deviceName, deviceId, temperature);
                } catch (NumberFormatException e) {
                    OUTPUT_LOG.add("Invalid temperature value");
                }
                break;
            case "SetAngle":
                try {
                    int angle = Integer.parseInt(parts[COMMAND_PARTS_VALUE_INDEX]);
                    setAngle(deviceName, deviceId, angle);
                } catch (NumberFormatException e) {
                    OUTPUT_LOG.add("Invalid angle value");
                }
                break;
            case "StartRecording":
                startRecording(deviceName, deviceId);
                break;
            case "StopRecording":
                stopRecording(deviceName, deviceId);
                break;
            default:
                OUTPUT_LOG.add("Invalid command");
        }
    }

    /**
     * Turns on the specified smart device.
     * @param deviceName The type of the device.
     * @param deviceId   The ID of the device to turn on.
     */
    private static void turnOnDevice(String deviceName, int deviceId) {
        SmartDevice device = DEVICES.get(deviceId);
        if (device.isOn()) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is already on");
        } else {
            device.turnOn();
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is on");
        }
    }

    /**
     * Turns off the specified smart device.
     * @param deviceName The type of the device.
     * @param deviceId   The ID of the device to turn off.
     */
    private static void turnOffDevice(String deviceName, int deviceId) {
        SmartDevice device = DEVICES.get(deviceId);
        if (!device.isOn()) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is already off");
        } else {
            device.turnOff();
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is off");
        }
    }

    /**
     * Sets the brightness level of a light device.
     * @param deviceName  The type of the device.
     * @param deviceId    The ID of the device to adjust.
     * @param level  The brightness level to set.
     */
    private static void setBrightness(String deviceName, int deviceId, String level) {
        SmartDevice device = DEVICES.get(deviceId);
        if (!(device instanceof Light)) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is not a light");
            return;
        }
        Light light = (Light) device;
        if (!level.equalsIgnoreCase("LOW") && !level.equalsIgnoreCase("MEDIUM") && !level.equalsIgnoreCase("HIGH")) {
            OUTPUT_LOG.add("The brightness can only be one of \"LOW\", \"MEDIUM\", or \"HIGH\"");
            return;
        }
        BrightnessLevel brightnessLevel = BrightnessLevel.valueOf(level.toUpperCase());
        light.setBrightness(brightnessLevel);
        OUTPUT_LOG.add("Light " + deviceId + " brightness level is set to " + level);
    }

    /**
     * Sets the color of a light device.
     * @param deviceName The type of the device.
     * @param deviceId   The ID of the device to adjust.
     * @param color      The color to set.
     */
    private static void setColor(String deviceName, int deviceId, String color) {
        SmartDevice device = DEVICES.get(deviceId);
        if (!(device instanceof Light)) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is not a light");
            return;
        }
        Light light = (Light) device;
        if (!color.equalsIgnoreCase("YELLOW") && !color.equalsIgnoreCase("WHITE")) {
            OUTPUT_LOG.add("The light color can only be \"YELLOW\" or \"WHITE\"");
            return;
        }
        LightColor lightColor = LightColor.valueOf(color.toUpperCase());
        light.setLightColor(lightColor);
        OUTPUT_LOG.add("Light " + deviceId + " color is set to " + color);
    }

    /**
     * Starts charging a chargeable smart device.
     * @param deviceName The type of the device.
     * @param deviceId   The ID of the device to start charging.
     */
    private static void startChargingDevice(String deviceName, int deviceId) {
        SmartDevice device = DEVICES.get(deviceId);
        Chargeable chargeableDevice = (Chargeable) device;
        if (chargeableDevice.isCharging()) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is already charging");
        } else {
            chargeableDevice.startCharging();
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is charging");
        }
    }

    /**
     * Stops charging a chargeable smart device.
     * @param deviceName The type of the device.
     * @param deviceId   The ID of the device to stop charging.
     */
    private static void stopChargingDevice(String deviceName, int deviceId) {
        SmartDevice device = DEVICES.get(deviceId);
        Chargeable chargeableDevice = (Chargeable) device;
        if (!chargeableDevice.isCharging()) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is not charging");
        } else {
            chargeableDevice.stopCharging();
            OUTPUT_LOG.add(deviceName + " " + deviceId + " stopped charging");
        }
    }

    /**
     * Sets the temperature of a heater device.
     * @param deviceName  The type of the device.
     * @param deviceId    The ID of the device to adjust.
     * @param temperature The temperature value to set.
     */
    private static void setTemperature(String deviceName, int deviceId, int temperature) {
        SmartDevice device = DEVICES.get(deviceId);
        if (!(device instanceof Heater)) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is not a heater");
            return;
        }
        Heater heater = (Heater) device;
        if (temperature < Heater.MIN_HEATER_TEMP || temperature > Heater.MAX_HEATER_TEMP) {
            OUTPUT_LOG.add("Heater " + deviceId + " temperature should be in the range ["
                    + Heater.MIN_HEATER_TEMP + ", " + Heater.MAX_HEATER_TEMP + "]");
            return;
        }
        heater.setTemperature(temperature);
        OUTPUT_LOG.add("Heater " + deviceId + " temperature is set to " + temperature);
    }

    /**
     * Sets the angle of a camera device.
     * @param deviceName The type of the device.
     * @param deviceId   The ID of the device to adjust.
     * @param angle      The angle value to set.
     */
    private static void setAngle(String deviceName, int deviceId, int angle) {
        SmartDevice device = DEVICES.get(deviceId);
        if (!(device instanceof Camera)) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is not a camera");
            return;
        }
        Camera camera = (Camera) device;
        if (angle < Camera.MIN_CAMERA_ANGLE || angle > Camera.MAX_CAMERA_ANGLE) {
            OUTPUT_LOG.add("Camera " + deviceId + " angle should be in the range ["
                    + Camera.MIN_CAMERA_ANGLE + ", " + Camera.MAX_CAMERA_ANGLE + "]");
            return;
        }
        camera.setCameraAngle(angle);
        OUTPUT_LOG.add("Camera " + deviceId + " angle is set to " + angle);
    }

    /**
     * Starts recording on a camera device.
     * @param deviceName The type of the device.
     * @param deviceId   The ID of the device to start recording.
     */
    private static void startRecording(String deviceName, int deviceId) {
        SmartDevice device = DEVICES.get(deviceId);
        if (!(device instanceof Camera)) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is not a camera");
            return;
        }
        Camera camera = (Camera) device;
        if (camera.isRecording()) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is already recording");
        } else {
            camera.startRecording();
            OUTPUT_LOG.add(deviceName + " " + deviceId + " started recording");
        }
    }

    /**
     * Stops recording on a camera device.
     * @param deviceName The type of the device.
     * @param deviceId   The ID of the device to stop recording.
     */
    private static void stopRecording(String deviceName, int deviceId) {
        SmartDevice device = DEVICES.get(deviceId);
        if (!(device instanceof Camera)) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is not a camera");
            return;
        }
        Camera camera = (Camera) device;
        if (!camera.isRecording()) {
            OUTPUT_LOG.add(deviceName + " " + deviceId + " is not recording");
        } else {
            camera.stopRecording();
            OUTPUT_LOG.add(deviceName + " " + deviceId + " stopped recording");
        }
    }

    /**
     * Displays the status of all devices in the system.
     * The status includes each device's type, ID, and current state.
     */
    private static void displayAllStatus() {
        for (SmartDevice device : DEVICES.values()) {
            OUTPUT_LOG.add(device.displayStatus());
        }
    }

    /**
     * Displays the collected log of output messages.
     * This method prints all logged messages to the console.
     */
    private static void displayOutputLog() {
        for (String logEntry : OUTPUT_LOG) {
            System.out.println(logEntry);
        }
    }
}

/**
 * The interface Controllable.
 */
interface Controllable {
    /**
     * Turn on boolean.
     * @return the boolean.
     */
    boolean turnOn();

    /**
     * Turn off boolean.
     * @return the boolean.
     */
    boolean turnOff();

    /**
     * Is on boolean.
     * @return the boolean.
     */
    boolean isOn();
}

/**
 * The interface Chargeable.
 */
interface Chargeable {
    /**
     * Is charging boolean.
     * @return the boolean.
     */
    boolean isCharging();

    /**
     * Start charging boolean.
     * @return the boolean.
     */
    boolean startCharging();

    /**
     * Stop charging boolean.
     * @return the boolean.
     */
    boolean stopCharging();
}

/**
 * The type Smart device.
 */
abstract class SmartDevice implements Controllable {
    /**
     * The Status.
     */
    protected Status status;
    /**
     * The Device id.
     */
    protected int deviceId;

    /**
     * Instantiates a new Smart device.
     * @param status   the status.
     * @param deviceId the device id.
     */
    SmartDevice(Status status, int deviceId) {
        this.status = status;
        this.deviceId = deviceId;
    }

    @Override
    public boolean turnOn() {
        if (status == Status.ON) {
            return false;
        }
        status = Status.ON;
        return true;
    }

    @Override
    public boolean turnOff() {
        if (status == Status.OFF) {
            return false;
        }
        status = Status.OFF;
        return true;
    }

    @Override
    public boolean isOn() {
        return status == Status.ON;
    }

    /**
     * Gets status.
     * @return the status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets status.
     * @param status the status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets device id.
     * @return the device id.
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * Check status access boolean.
     * @return the boolean.
     */
    public boolean checkStatusAccess() {
        return status == Status.ON;
    }

    /**
     * Display status string.
     * @return the string.
     */
    public abstract String displayStatus();
}

/**
 * The type Light.
 */
class Light extends SmartDevice implements Chargeable {
    private LightColor color;
    private BrightnessLevel brightness;
    private boolean charging;

    /**
     * Instantiates a new Light.
     * @param status     the status.
     * @param deviceId   the device id.
     * @param color      the color.
     * @param brightness the brightness.
     * @param charging   the charging.
     */
    Light(Status status, int deviceId, LightColor color, BrightnessLevel brightness, boolean charging) {
        super(status, deviceId);
        this.color = color;
        this.brightness = brightness;
        this.charging = charging;
    }

    /**
     * Gets light color.
     * @return the light color.
     */
    public LightColor getLightColor() {
        return color;
    }

    /**
     * Sets light color.
     * @param color the color.
     */
    public void setLightColor(LightColor color) {
        this.color = color;
    }

    /**
     * Gets brightness.
     * @return the brightness.
     */
    public BrightnessLevel getBrightness() {
        return brightness;
    }

    /**
     * Sets brightness.
     * @param brightness the brightness.
     */
    public void setBrightness(BrightnessLevel brightness) {
        this.brightness = brightness;
    }

    @Override
    public boolean isCharging() {
        return charging;
    }

    @Override
    public boolean startCharging() {
        if (!this.charging) {
            this.charging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean stopCharging() {
        if (this.charging) {
            this.charging = false;
            return true;
        }
        return false;
    }

    @Override
    public String displayStatus() {
        return "Light " + deviceId + " is " + status + ", the color is " + color + ", the charging status is "
                + charging + ", and the brightness level is " + brightness + ".";
    }
}

/**
 * The type Camera.
 */
class Camera extends SmartDevice implements Chargeable {
    /**
     * The Max camera angle.
     */
    static final int MAX_CAMERA_ANGLE = 60;
    /**
     * The Min camera angle.
     */
    static final int MIN_CAMERA_ANGLE = -60;
    private boolean charging;
    private boolean recording;
    private int angle;

    /**
     * Instantiates a new Camera.
     * @param status       the status.
     * @param deviceId     the device id.
     * @param charging     the charging.
     * @param recording    the recording.
     * @param angle        the angle.
     * @param isChargeable the is chargeable.
     */
    Camera(Status status, int deviceId, boolean charging, boolean recording, int angle, boolean isChargeable) {
        super(status, deviceId);
        this.charging = charging;
        this.recording = recording;
        this.angle = angle;
    }

    @Override
    public boolean isCharging() {
        return charging;
    }

    @Override
    public boolean startCharging() {
        if (!this.charging) {
            this.charging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean stopCharging() {
        if (this.charging) {
            this.charging = false;
            return true;
        }
        return false;
    }

    /**
     * Is recording boolean.
     * @return the boolean.
     */
    public boolean isRecording() {
        return recording;
    }

    /**
     * Start recording.
     */
    public void startRecording() {
        this.recording = true;
    }

    /**
     * Stop recording.
     */
    public void stopRecording() {
        this.recording = false;
    }

    /**
     * Gets angle.
     * @return the angle.
     */
    public int getAngle() {
        return angle;
    }

    /**
     * Sets camera angle.
     * @param angle the angle.
     */
    public void setCameraAngle(int angle) {
        this.angle = angle;
    }

    @Override
    public String displayStatus() {
        return "Camera " + deviceId + " is " + status + ", the angle is " + angle + ", the charging status is "
                + charging + ", and the recording status is " + recording + ".";
    }
}

/**
 * The type Heater.
 */
class Heater extends SmartDevice {
    /**
     * The Max heater temp.
     */
    static final int MAX_HEATER_TEMP = 30;
    /**
     * The Min heater temp.
     */
    static final int MIN_HEATER_TEMP = 15;
    private int temperature;

    /**
     * Instantiates a new Heater.
     * @param status      the status.
     * @param deviceId    the device id.
     * @param temperature the temperature.
     */
    Heater(Status status, int deviceId, int temperature) {
        super(status, deviceId);
        this.temperature = temperature;
    }

    /**
     * Gets temperature.
     * @return the temperature.
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * Sets temperature.
     * @param temperature the temperature.
     */
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public String displayStatus() {
        return "Heater " + deviceId + " is " + status + " and the temperature is " + temperature + ".";
    }
}

/**
 * The enum Status.
 */
enum Status {
    /**
     * Off status.
     */
    OFF,
    /**
     * On status.
     */
    ON
}

/**
 * The enum Light color.
 */
enum LightColor {
    /**
     * White light color.
     */
    WHITE,
    /**
     * Yellow light color.
     */
    YELLOW
}

/**
 * The enum Brightness level.
 */
enum BrightnessLevel {
    /**
     * Low brightness level.
     */
    LOW,
    /**
     * Medium brightness level.
     */
    MEDIUM,
    /**
     * High brightness level.
     */
    HIGH
}
