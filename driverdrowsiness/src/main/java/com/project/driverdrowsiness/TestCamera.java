package com.project.driverdrowsiness;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.Toolkit;
import java.time.LocalDateTime;

public class TestCamera {

    static {
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java4120.dll");
    }

    public static void main(String[] args) {

        int ALERT_THRESHOLD = 8;

        VideoCapture cap = new VideoCapture(0);

        if (!cap.isOpened()) {
            System.out.println("Camera not opened ❌");
            return;
        }

        System.out.println("Camera working ✅");

        CascadeClassifier faceDetector =
            new CascadeClassifier("C:\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_default.xml");

        CascadeClassifier eyeDetector =
            new CascadeClassifier("C:\\opencv\\sources\\data\\haarcascades\\haarcascade_eye.xml");

        int closedEyesFrames = 0;
        long lastAlertTime = 0;

        // 🔥 NEW FEATURES
        int alertCount = 0;
        long startTime = System.currentTimeMillis();

        Mat frame = new Mat();

        while (true) {

            cap.read(frame);

            if (frame.empty()) {
                System.out.println("No frame ❌");
                break;
            }

            Mat gray = new Mat();
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(gray, faces);

            int eyeCountGlobal = 0;

            for (Rect rect : faces.toArray()) {

                Imgproc.rectangle(frame, rect, new Scalar(0,255,0), 2);

                Mat faceROI = gray.submat(rect);

                MatOfRect eyes = new MatOfRect();
                eyeDetector.detectMultiScale(faceROI, eyes, 1.1, 5, 0,
                        new Size(30, 30), new Size());

                eyeCountGlobal = eyes.toArray().length;

                for (Rect eye : eyes.toArray()) {
                    Imgproc.rectangle(frame,
                        new Point(rect.x + eye.x, rect.y + eye.y),
                        new Point(rect.x + eye.x + eye.width,
                                  rect.y + eye.y + eye.height),
                        new Scalar(255,0,0), 2);
                }

                if (eyes.toArray().length <= 1) {
                    closedEyesFrames++;
                } else {
                    closedEyesFrames = 0;
                }
            }

            // 🚨 ALERT
            if (closedEyesFrames > ALERT_THRESHOLD) {

                Imgproc.rectangle(frame,
                    new Point(0, 0),
                    new Point(frame.cols(), 90),
                    new Scalar(0, 0, 255), -1);

                Imgproc.putText(frame, "WAKE UP! Stay Alert!",
                    new Point(50,40),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0, new Scalar(255,255,255), 3);

                // 🔔 VISUAL BEEP
                Imgproc.putText(frame, "ALERT SOUND ON",
                    new Point(50,75),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    0.7, new Scalar(255,255,0), 2);

                if (System.currentTimeMillis() - lastAlertTime > 2000) {

                    Toolkit.getDefaultToolkit().beep();
                    lastAlertTime = System.currentTimeMillis();

                    // 📸 Save processed image
                    String filename = "alert_" + System.currentTimeMillis() + ".png";
                    Imgcodecs.imwrite(filename, frame);

                    alertCount++; // 🔥 counter
                }
            }

            // 🔝 Title + System Active
            Imgproc.putText(frame, "Driver Alertness Monitoring System",
                new Point(10,25),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.7, new Scalar(255,255,255), 2);

            Imgproc.putText(frame, "System Active",
                new Point(frame.cols() - 160, 25),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.6, new Scalar(0,255,0), 2);

            // 🔻 Bottom panel
            Imgproc.rectangle(frame,
                new Point(0, frame.rows() - 120),
                new Point(300, frame.rows()),
                new Scalar(0, 0, 0), -1);

            String status = (closedEyesFrames > ALERT_THRESHOLD) ? "DROWSY" : "AWAKE";

            Scalar statusColor = (closedEyesFrames > ALERT_THRESHOLD)
                    ? new Scalar(0,0,255)
                    : new Scalar(0,255,0);

            Imgproc.putText(frame, "Status: " + status,
                new Point(10, frame.rows() - 90),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.7, statusColor, 2);

            Imgproc.putText(frame, "Faces: " + faces.toArray().length,
                new Point(10, frame.rows() - 65),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.6, new Scalar(255,255,255), 2);

            Imgproc.putText(frame, "Eyes: " + eyeCountGlobal,
                new Point(10, frame.rows() - 40),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.6, new Scalar(255,255,255), 2);

            // 🔥 Alert Counter
            Imgproc.putText(frame, "Alerts: " + alertCount,
                new Point(10, frame.rows() - 15),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.6, new Scalar(0,0,255), 2);

            // 🔥 Session Timer
            long seconds = (System.currentTimeMillis() - startTime) / 1000;
            Imgproc.putText(frame, "Time: " + seconds + "s",
                new Point(frame.cols() - 150, frame.rows() - 20),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.6, new Scalar(255,255,255), 2);

         // 🔥 Current Date-Time
            String time = LocalDateTime.now().toString();
            Imgproc.putText(frame, time,
                new Point(frame.cols() - 300, frame.rows() - 50),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.4, new Scalar(255,255,255), 1);

            HighGui.imshow("Driver Alertness Monitoring System", frame);

            if (HighGui.waitKey(30) == 27) break;
        }

        cap.release();
        HighGui.destroyAllWindows();
    }
}