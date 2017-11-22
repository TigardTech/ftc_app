package com.arinerron.ftc;

import com.qualcomm.robotcore.util.ElapsedTime;

public class Driver {
    private Robot robot = null;

    public static final int STRAIGHT = 0;
    public static final int ROTATE = -1;
    public static final int NINETY = 1;

    public Driver(Robot robot) {
        this.robot = robot;

        this.getRobot().getMotor1().invert(true);
        this.getRobot().getMotor2().invert(true);
    }

    private final double[] straight /*0*/ = {1, 0.2, 0.7, 0.3}, ff /*1*/ = {0.6, 0.6, 0.3, 0.6}, ninety /*-1*/ = {0.3, 0.9, 0, 1};
    private int dir = 0;

    public void point(int direction) {
        if(direction == NINETY) {
            /*
             * _   _
             *
             * _   _
             *
             */
            this.getRobot().getServo1().setPosition(ninety[0]);
            this.getRobot().getServo2().setPosition(ninety[1]);
            this.getRobot().getServo3().setPosition(ninety[2]);
            this.getRobot().getServo4().setPosition(ninety[3]);
        } else if(direction == ROTATE) {
            /*
             * /   \
             *
             * \   /
             *
             */
            this.getRobot().getServo1().setPosition(ff[0]);
            this.getRobot().getServo2().setPosition(ff[1]);
            this.getRobot().getServo3().setPosition(ff[2]);
            this.getRobot().getServo4().setPosition(ff[3]);
        } else {
            /*
             * |   |
             *
             * |   |
             *
             */
            this.getRobot().getServo1().setPosition(straight[0]);
            this.getRobot().getServo2().setPosition(straight[1]);
            this.getRobot().getServo3().setPosition(straight[2]);
            this.getRobot().getServo4().setPosition(straight[3]);
        }

        dir = direction;
    }

    public void drive(double x) {
        /* dir: 0=straight, 1=ff, -1=90 */
        if (dir == ROTATE) {
            // ff
            this.getRobot().getMotor1().setPower(x);
            this.getRobot().getMotor2().setPower(-x);
            this.getRobot().getMotor3().setPower(x);
            this.getRobot().getMotor4().setPower(-x);
        } else if (dir == NINETY) {
            // ninety = invert some of these   l a t e r  ...
            this.getRobot().getMotor1().setPower(-x);
            this.getRobot().getMotor2().setPower(-x); // inverted
            this.getRobot().getMotor3().setPower(x); // inverted
            this.getRobot().getMotor4().setPower(x);
        } else if (dir == STRAIGHT) {
            // straight
            this.getRobot().getMotor1().setPower(-x);
            this.getRobot().getMotor2().setPower(x); // inverted
            this.getRobot().getMotor3().setPower(x); // inverted
            this.getRobot().getMotor4().setPower(-x);
        }
    }

    public void drive(double x, double inches) {
        int inches_per_tick = 1310;

        double tics = (inches / CONSTANT) * 1310;
    }

    public void stop() {
        this.getRobot().getMotor1().setPower(0);
        this.getRobot().getMotor2().setPower(0);
        this.getRobot().getMotor3().setPower(0);
        this.getRobot().getMotor4().setPower(0);
    }

    public void check() {
        if(this.getRobot().getServo1() != null && Double.isNaN(this.getRobot().getServo1().getPosition()))
            this.getRobot().getServo1().setPosition(straight[0]);
        if(this.getRobot().getServo2() != null && Double.isNaN(this.getRobot().getServo2().getPosition()))
            this.getRobot().getServo2().setPosition(straight[1]);
        if(this.getRobot().getServo3() != null && Double.isNaN(this.getRobot().getServo3().getPosition()))
            this.getRobot().getServo3().setPosition(straight[2]);
        if(this.getRobot().getServo4() != null && Double.isNaN(this.getRobot().getServo4().getPosition()))
            this.getRobot().getServo4().setPosition(straight[3]);
    }

    public static boolean isZero(double x) {
        return x < Constants.TRIGGER_THRESHOLD && x > -Constants.TRIGGER_THRESHOLD;
    }

    public static boolean isZero(double x, double threshold) {
        return x < threshold && x > -threshold;
    }

    public static final double CONSTANT = (4 * Math.PI);

    public static double getAngle(double x, double y) {
        if(isZero(x) && isZero(y))
            return 0;
        return Math.toDegrees(1.5 * Math.PI - Math.atan2(y, x)) - 180; // supposed to be y,x
    }

    public void rotate(double degrees) {
        ElapsedTime timer = new ElapsedTime();
        while(timer.seconds() < 5 && this.getRobot().getGyroSensor().isCalibrating()); // max 5 secs for calibration.

        this.getRobot().reset();

        int olddir = -10;
        // check if turned or not
        if(this.dir != ROTATE) {
            olddir = this.dir;
            timer.reset();
            this.point(ROTATE);
            while(timer.seconds() < 1.5); // delay to wait for wheels to turn
        }

        // generate data
        double heading = this.getRobot().getGyroSensor().getHeading();
        double target = heading + degrees;
        target = (double)((double)((double) (target % 360) + 360) + 360); // I don't even know...

        if(degrees > 0) {
            // rotate clockwise
            this.drive(1);
        } else {
            // rotate counterclockwise
            this.drive(-1);
        }

        int distance = Math.abs((int)(target - heading)); // calculate distance from target

        timer.reset();
        while(timer.seconds() < 10 && distance < 5) { // 5 is the threshold of angles
            distance = Math.abs((int)(target - this.getRobot().getGyroSensor().getHeading())); // check if done rotating or not
        }

        // reset everything
        this.getRobot().reset();
        if(olddir != -10)
            this.point(olddir);
    }

    private Double[] opened = new Double[] {1.0d, 0.4d, 1d};
    private Double[] closed = new Double[] {0.4d, 1.0d, 0d};

    /* open: true = open, false = closed */
    public void setServo(String servo, boolean open) {

        double pos = 0.5;
        switch(servo.trim().toLowerCase()) {
            case "arm1":
                if(open)
                    pos = opened[0];
                else
                    pos = closed[0];
                this.getRobot().getServoArm1().setPosition(pos);

                break;
            case "arm2":
                if(open)
                    pos = opened[1];
                else
                    pos = closed[1];
                this.getRobot().getServoArm2().setPosition(pos);

                break;
            case "arme":
                if(open)
                    pos = opened[2];
                else
                    pos = closed[2];
                this.getRobot().getServoArmE().setPosition(pos);

                break;
            default:
                System.err.println("lol that servo no exists!!!");
        }
    }

    public boolean isColor(double r, double g, double b, String name) {
        name = name.toLowerCase(); // get the lower case version of the name

        switch(name) {
            case "red":
                if(r >= 0.5)
                    return true; // it is red
                break;
            case "green":
                if(g >= 0.5)
                    return true; // it is green
                break;
            case "blue":
                if(b >= 0.5)
                    return true; // it is blue
                break;
            case "white":
                if(r > 0.5 && g > 0.5 && b > 0.5)
                    return true;
                break;
            case "black":
                if(r < 0.5 && g < 0.5 && b < 0.5)
                    return true;
                break;
            default:
                // status("Unknown color (" + r + ", " + g + ", " + b + ")."); // unknown color; will get annoying if it is spamming console.
                return false;
        }

        return false; // unknown color
    }

    public Robot getRobot() {
        return this.robot;
    }
}
