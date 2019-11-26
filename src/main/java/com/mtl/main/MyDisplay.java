package com.mtl.main;

import com.mtl.utils.WinUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;

/**
 * Created by MTL on 2019/11/23
 */
public class MyDisplay {
    private JFrame frame = new JFrame("EasyWork");
    private MyService myService;
    private JLabel durTime;
    private JLabel nextTime;
    private int mouseAtX;
    private int mouseAtY;
    private JPanel main;

    public void display() {

        initFrame();

        Container contentPane = frame.getContentPane();
        main = new JPanel();

        durTime = new JLabel("40m00s   ");
        durTime.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                myService.haveARest(10);
            }
        });
        nextTime = new JLabel("16:23");

        main.add(durTime);
        main.add(nextTime);

        contentPane.add(main);
        myService = new MyService();
        myService.initNextTime();

        frame.setVisible(true);

        if (myService.initNextTime()) {
            new Thread(myService).start();
        }
    }

    private void initFrame() {

        frame.setSize(100, 28);
        frame.setResizable(true);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setType(JFrame.Type.UTILITY);
        frame.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                /*
                 * 获取点击鼠标时的坐标
                 */
                mouseAtX = e.getPoint().x;
                mouseAtY = e.getPoint().y;
            }
        });
        frame.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                frame.setLocation((e.getXOnScreen() - mouseAtX), (e.getYOnScreen() - mouseAtY));//设置拖拽后，窗口的位置
            }
        });

        // 初始化托盘图标
        tray();
    }

    private void tray() {//创建一个托盘图标对象
        TrayIcon icon =
                null;
        try {
            icon = new TrayIcon(WinUtil.imageIcon);
            icon.setImageAutoSize(true);

            //创建弹出菜单
            PopupMenu menu = new PopupMenu();
            //添加一个用于退出的按钮
            MenuItem item = new MenuItem("exit");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            menu.add(item);

            //添加弹出菜单到托盘图标
            icon.setPopupMenu(menu);
            icon.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    System.out.println("---");
                    frame.setAlwaysOnTop(false);
                    frame.repaint();
                    frame.setAlwaysOnTop(true);
                    frame.repaint();
                }
            });
            SystemTray tray = SystemTray.getSystemTray();//获取系统托盘
            tray.add(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyService implements Runnable {

        private String msStr = "07:20,08:00,08:10,08:50,09:00,09:45,09:55,10:40,10:50,11:35,11:45,12:30,12:40,13:20,13:30,14:10,14:20,15:00,15:10,15:50,16:00,16:40,16:50,17:30,17:50,18:30,18:40,19:20,19:30,20:10,20:20,21:00,21:20,22:00";
        private int[] ms;
        private boolean isRunning = false;
        private int nextTime = 0;
        private int nextIndex = 0;
        private int dt = 0;

        MyService() {
            String[] mss = msStr.split(",");
            ms = new int[mss.length];
            for (int i = 0; i < mss.length; i++) {
                String s = mss[i];
                String[] hm = s.split(":");
                int h = Integer.valueOf(hm[0]);
                int m = Integer.valueOf(hm[1]);
                ms[i] = h * 60 + m;
            }
        }

        @Override
        public void run() {
            if (initNextTime()) {

                while (isRunning) {
                    int curTime = getCurTime();
                    if (curTime >= nextTime) {
                        // 更新nextTime,dt的值
                        initNextTime();
                        // 发送通知
                        WinUtil.sendNotification("attention", String.format("next: \n%02d m\n %s", dt, toHHMM(nextTime)), "");
                    }
                    setDurTime(toMMSS(nextTime * 60 - getCurTimeSecond()));
                    MyDisplay.this.frame.repaint();
                    //System.out.println(getCurTimeStr());

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        private String toMMSS(int time) {
            return String.format("%02dm%02ds", time / 60, time % 60);
        }

        /**
         * 初始化nextTime dt nextIndex
         *
         * @return
         */
        private boolean initNextTime() {
            int curTime = getCurTime();
            for (int i = 0; i < ms.length; i++) {
                int time = ms[i];
                if (curTime < time) {
                    setIndex(i);
                    dt = time - curTime;
                    nextTime = time;
                    isRunning = true;
                    setShow(nextTime, toMMSS(nextTime * 60 - getCurTimeSecond()));
                    return true;
                }
            }
            return false;
        }

        private void setIndex(int i) {
            nextIndex = i;
            if (nextIndex % 2 == 1)
                main.setBackground(Color.ORANGE);
            else
                main.setBackground(Color.WHITE);
        }

        private void setShow(int nextTime, String durTime) {
            setNextTimeShow(nextTime);
            setDurTime(durTime);
        }

        private void setNextTimeShow(int nextTime) {
            MyDisplay.this.nextTime.setText(toHHMM(nextTime));
        }

        private void setDurTime(String durTime) {

            MyDisplay.this.durTime.setText(String.format("%s   ", durTime));
        }

        private void updateNextTime() {
            setIndex(nextIndex + 1);
            if (nextIndex == ms.length) {
                stop();
            }
            nextTime = ms[nextIndex];
            dt = nextTime - getCurTime();
            setNextTimeShow(nextTime);
        }

        private void stop() {
            isRunning = false;
        }

        private String toHHMM(int curTime) {
            int hour = curTime / 60;
            int minute = curTime % 60;
            return String.format("%02d:%02d", hour, minute);
        }

        private int getCurTime() {
            Calendar instance = Calendar.getInstance();
            int hour = instance.get(Calendar.HOUR_OF_DAY);
            int minute = instance.get(Calendar.MINUTE);
            return hour * 60 + minute;
        }

        private int getCurTimeSecond() {
            Calendar instance = Calendar.getInstance();
            int hour = instance.get(Calendar.HOUR_OF_DAY);
            int minute = instance.get(Calendar.MINUTE);
            int second = instance.get(Calendar.SECOND);
            return (hour * 60 + minute) * 60 + second;
        }

        public void haveARest(int minute) {
            int i = getCurTime() + minute;
            if (i > nextTime && nextIndex % 2 == 1){
                updateNextTime();
            }else {
                nextTime = getCurTime() + minute;
                setNextTimeShow(nextTime);
                main.setBackground(Color.WHITE);
            }
        }
    }
}
