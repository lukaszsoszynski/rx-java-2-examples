package com.impaqgroup.training.reactive.in00async;

import java.awt.event.ActionEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import org.junit.Test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GuiAsyncTest {

    private boolean timerActive;
    private int counter;
    private JLabel label;

    @Test
    public void asynchronousProgramingModel() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        log.info("Starting GUI application");
        SwingUtilities.invokeLater(() -> runSwingApplication(countDownLatch));
        countDownLatch.await();
    }

    private void runSwingApplication(CountDownLatch countDownLatch){
        this.timerActive = false;
        JFrame frame = new JFrame("Asynchronous programing model.");
        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
        this.label = new JLabel("Current number: ");
        panel.add(label);
        JButton startButton = new JButton("Start");
        startButton.addActionListener(this::onStartButtonClicked);
        panel.add(startButton);
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(this::onStopButtonClicked);
        panel.add(stopButton);
        JButton resetButton = new JButton("Reset");
        panel.add(resetButton);
        resetButton.addActionListener(this::onResetButtonClicked);
        JButton blockButton = new JButton("Block event dispatching thread");
        blockButton.addActionListener(this::onBlockButtonClicked);
        panel.add(blockButton);
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(a -> onExitButtonClicked(countDownLatch));
        panel.add(exitButton);
        Timer  timer = new Timer(1000, this::onTimerEvent);
        timer.setInitialDelay(0);
        timer.start();
        frame.pack();
        frame.setVisible(true);
        log.info("GUI elements created");
    }

    private void onResetButtonClicked(ActionEvent actionEvent) {
        log.info("Reset button clicked.");
        this.counter = 0;
        updateLabelText();
    }

    private void onStopButtonClicked(ActionEvent actionEvent) {
        log.info("Stop button clicked");
        this.timerActive = false;
    }

    private void onStartButtonClicked(ActionEvent actionEvent) {
        log.info("Start button clicked");
        this.timerActive = true;
    }

    private void onExitButtonClicked(CountDownLatch countDownLatch) {
        log.info("Exit button clicked");
        countDownLatch.countDown();
    }

    @SneakyThrows
    private void onBlockButtonClicked(ActionEvent actionEvent) {
        log.info("event dispatching thread blocked for 10 sec.");
        TimeUnit.SECONDS.sleep(10);
    }

    private void onTimerEvent(ActionEvent actionEvent) {
        if(timerActive){
            updateLabelText();
            log.info("Current number updated {}", counter);
        }
    }

    private void updateLabelText() {
        label.setText(String.format("Current number: %d", counter++));
    }

}
