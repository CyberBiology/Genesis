import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;


// Основной класс программы.
public class World extends JFrame {

    public int width;
    public int height;
    public int zoom;
    public int sealevel;
    public int drawstep;
    public int[][] map;    //Карта мира
    public int[] mapInGPU;    //Карта для GPU
    public Image mapbuffer = null;
    public Bot[][] matrix;    //Матрица мира
    public Bot zerobot = new Bot();
    public Bot currentbot;
    public int generation;
    public int population;
    public int organic;

    Image buffer = null;

    Thread thread = null;
    boolean started = true; // поток работает?
    JPanel canvas = new JPanel() {
    	public void paint(Graphics g) {
    		g.drawImage(buffer, 0, 0, null);
    	}
    };

    JPanel paintPanel = new JPanel(new FlowLayout());

    JLabel generationLabel = new JLabel(" Generation: 0 ");
    JLabel populationLabel = new JLabel(" Population: 0 ");
    JLabel organicLabel = new JLabel(" Organic: 0 ");

    JSlider perlinSlider = new JSlider (JSlider.HORIZONTAL, 0, 480, 160);
    JButton mapButton = new JButton("Create Map");
    JSlider sealevelSlider = new JSlider (JSlider.HORIZONTAL, 0, 256, 145);
    JButton startButton = new JButton("Start/Stop");
    JSlider drawstepSlider = new JSlider (JSlider.HORIZONTAL, 0, 40, 10);


    JRadioButton baseButton = new JRadioButton("Base", true);
    JRadioButton combinedButton = new JRadioButton("Combined", false);
    JRadioButton energyButton = new JRadioButton("Energy", false);
    JRadioButton mineralButton = new JRadioButton("Minerals", false);
    JRadioButton ageButton = new JRadioButton("Age", false);

    public World() {
    	
        simulation = this;

        zoom = 1;
        sealevel = 145;
        drawstep = 10;

        setTitle("Genesis 1.2.0");
        setSize(new Dimension(1800, 900));
        Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize(), fSize = getSize();
        if (fSize.height > sSize.height) fSize.height = sSize.height;
        if (fSize.width  > sSize.width) fSize.width = sSize.width;
        //setLocation((sSize.width - fSize.width)/2, (sSize.height - fSize.height)/2);
        setSize(new Dimension(sSize.width, sSize.height));
        
        
        setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);

        Container container = getContentPane();

        paintPanel.setLayout(new BorderLayout());// у этого лейаута приятная особенность - центральная часть растягивается автоматически
        paintPanel.add(canvas, BorderLayout.CENTER);// добавляем нашу карту в центр
        container.add(paintPanel);

        JPanel statusPanel = new JPanel(new FlowLayout());
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        container.add(statusPanel, BorderLayout.SOUTH);

        generationLabel.setPreferredSize(new Dimension(140, 18));
        generationLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.add(generationLabel);
        populationLabel.setPreferredSize(new Dimension(140, 18));
        populationLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.add(populationLabel);
        organicLabel.setPreferredSize(new Dimension(140, 18));
        organicLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.add(organicLabel);


        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(1);
//        toolbar.setBorderPainted(true);
//        toolbar.setBorder(BorderFactory.createLoweredBevelBorder());
        container.add(toolbar, BorderLayout.WEST);

        JLabel slider1Label = new JLabel("Map scale");
        toolbar.add(slider1Label);

        perlinSlider.setMajorTickSpacing(160);
        perlinSlider.setMinorTickSpacing(80);
        perlinSlider.setPaintTicks(true);
        perlinSlider.setPaintLabels(true);
        perlinSlider.setPreferredSize(new Dimension(100, perlinSlider.getPreferredSize().height));
        perlinSlider.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        toolbar.add(perlinSlider);

        mapButton.addActionListener(new mapButtonAction());
        toolbar.add(mapButton);

        JLabel slider2Label = new JLabel("Sea level");
        toolbar.add(slider2Label);

        sealevelSlider.addChangeListener(new sealevelSliderChange());
        sealevelSlider.setMajorTickSpacing(128);
        sealevelSlider.setMinorTickSpacing(64);
        sealevelSlider.setPaintTicks(true);
        sealevelSlider.setPaintLabels(true);
        sealevelSlider.setPreferredSize(new Dimension(100, sealevelSlider.getPreferredSize().height));
        sealevelSlider.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        toolbar.add(sealevelSlider);

        startButton.addActionListener(new startButtonAction());
        toolbar.add(startButton);

        JLabel slider3Label = new JLabel("Draw step");
        toolbar.add(slider3Label);

        drawstepSlider.addChangeListener(new drawstepSliderChange());
        drawstepSlider.setMajorTickSpacing(10);
//        drawstepSlider.setMinimum(1);
//        drawstepSlider.setMinorTickSpacing(64);
        drawstepSlider.setPaintTicks(true);
        drawstepSlider.setPaintLabels(true);
        drawstepSlider.setPreferredSize(new Dimension(100, sealevelSlider.getPreferredSize().height));
        drawstepSlider.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        toolbar.add(drawstepSlider);


        ButtonGroup group = new ButtonGroup();
        group.add(baseButton);
        group.add(combinedButton);
        group.add(energyButton);
        group.add(mineralButton);
        group.add(ageButton);
        toolbar.add(baseButton);
        toolbar.add(combinedButton);
        toolbar.add(energyButton);
        toolbar.add(mineralButton);
        toolbar.add(ageButton);

        this.pack();
        this.setVisible(true);
        setExtendedState(MAXIMIZED_BOTH);
    }

    class sealevelSliderChange implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            sealevel = sealevelSlider.getValue();
            if (map != null) {
                paintMapView();
                paint1();
            }
        }
    }

    class drawstepSliderChange implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            int ds = drawstepSlider.getValue();
            if (ds == 0) ds = 1;
            drawstep = ds;
        }
    }

    class mapButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            width = canvas.getWidth() / zoom;    // Ширина доступной части экрана для рисования карты
            height = canvas.getHeight() / zoom;
            generateMap((int) (Math.random() * 10000));
            generateAdam();
            paintMapView();
            paint1();
        }
    }
    class startButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	if(thread==null) {
        	    perlinSlider.setEnabled(false);
        	    mapButton.setEnabled(false);
        		thread	= new Worker(); // создаем новый поток
        		thread.start();
        	} else {
        		started = false;        //Выставляем влаг
        		thread = null;
                perlinSlider.setEnabled(true);
                mapButton.setEnabled(true);
        	}
        }
    }

    public void paintMapView() {
        int mapred;
        int mapgreen;
        int mapblue;
        mapbuffer = canvas.createImage(width * zoom, height * zoom); // ширина - высота картинки
        Graphics g = mapbuffer.getGraphics();

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final int[] rgb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < rgb.length; i++) {
            if (mapInGPU[i] < sealevel) {                     // подводная часть
                mapred = 5;
                mapblue = 140 - (sealevel - mapInGPU[i]) * 3;
                mapgreen = 150 - (sealevel - mapInGPU[i]) * 10;
                if (mapgreen < 10) mapgreen = 10;
                if (mapblue < 20) mapblue = 20;
            } else {                                        // надводная часть
                mapred = (int)(150 + (mapInGPU[i] - sealevel) * 2.5);
                mapgreen = (int)(100 + (mapInGPU[i] - sealevel) * 2.6);
                mapblue = 50 + (mapInGPU[i] - sealevel) * 3;
                if (mapred > 255) mapred = 255;
                if (mapgreen > 255) mapgreen = 255;
                if (mapblue > 255) mapblue = 255;
            }
            rgb[i] = (mapred << 16) | (mapgreen << 8) | mapblue;
        }
        g.drawImage(image, 0, 0, null);


    }


//    @Override
    public void paint1() {

        Image buf = canvas.createImage(width * zoom, height * zoom); //Создаем временный буфер для рисования
        Graphics g = buf.getGraphics(); //подеменяем графику на временный буфер
        g.drawImage(mapbuffer, 0, 0, null);

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final int[] rgb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        population = 0;
        organic = 0;
        int mapred, mapgreen, mapblue;

        while (currentbot != zerobot) {
            if (currentbot.alive == 3) {                      // живой бот
                if (baseButton.isSelected()) {
                    rgb[currentbot.y * width + currentbot.x] = (255 << 24) | (currentbot.c_red << 16) | (currentbot.c_green << 8) | currentbot.c_blue;
                } else if (energyButton.isSelected()) {
                    mapgreen = 255 - (int) (currentbot.health * 0.25);
                    if (mapgreen < 0) mapgreen = 0;
                    rgb[currentbot.y * width + currentbot.x] = (255 << 24) | (255 << 16) | (mapgreen << 8) | 0;
                } else if (mineralButton.isSelected()) {
                    mapblue = 255 - (int) (currentbot.mineral * 0.5);
                    if (mapblue < 0) mapblue = 0;
                    rgb[currentbot.y * width + currentbot.x] = (255 << 24) | (0 << 16) | (255 << 8) | mapblue;
                } else if (combinedButton.isSelected()) {
                    mapgreen = (int) (currentbot.c_green * (1 - currentbot.health * 0.0005));
                    if (mapgreen < 0) mapgreen = 0;
                    mapblue = (int) (currentbot.c_blue * (0.8 - currentbot.mineral * 0.0005));
                    rgb[currentbot.y * width + currentbot.x] = (255 << 24) | (currentbot.c_red << 16) | (mapgreen << 8) | mapblue;
                } else if (ageButton.isSelected()) {
                    mapred = 255 - (int) (Math.sqrt(currentbot.age) * 4);
                    if (mapred < 0) mapred = 0;
                    rgb[currentbot.y * width + currentbot.x] = (255 << 24) | (mapred << 16) | (0 << 8) | 255;
                }
                population++;
            } else if (currentbot.alive == 1) {                                            // органика, известняк, коралловые рифы
                if (map[currentbot.x][currentbot.y] < sealevel) {                     // подводная часть
                    mapred = 20;
                    mapblue = 160 - (sealevel - map[currentbot.x][currentbot.y]) * 2;
                    mapgreen = 170 - (sealevel - map[currentbot.x][currentbot.y]) * 4;
                    if (mapblue < 40) mapblue = 40;
                    if (mapgreen < 20) mapgreen = 20;
                } else {                                    // скелетики, трупики на суше
                    mapred = (int) (80 + (map[currentbot.x][currentbot.y] - sealevel) * 2.5);   // надводная часть
                    mapgreen = (int) (60 + (map[currentbot.x][currentbot.y] - sealevel) * 2.6);
                    mapblue = 30 + (map[currentbot.x][currentbot.y] - sealevel) * 3;
                    if (mapred > 255) mapred = 255;
                    if (mapblue > 255) mapblue = 255;
                    if (mapgreen > 255) mapgreen = 255;
                }
                rgb[currentbot.y * width + currentbot.x] = (255 << 24) | (mapred << 16) | (mapgreen << 8) | mapblue;
                organic++;
            }
            currentbot = currentbot.next;
        }
        currentbot = currentbot.next;

        g.drawImage(image, 0, 0, null);

        generationLabel.setText(" Generation: " + String.valueOf(generation));
        populationLabel.setText(" Population: " + String.valueOf(population));
        organicLabel.setText(" Organic: " + String.valueOf(organic));

        buffer = buf;
        canvas.repaint();
    }


    class Worker extends Thread {
        public void run() {
            started	= true;         // Флаг работы потока, если false  поток заканчивает работу
            while (started) {       // обновляем матрицу
//                for (int y = 0; y < height; y++) {
//                    for (int x = 0; x < width; x++) {
//                        if (matrix[x][y] != null) {
//                            if (matrix[x][y].alive == 3) {
//                                if (matrix[x][y].lastgeneration < generation) matrix[x][y].step();        // выполняем шаг бота
//                            }
//                        }
//                    }
//                }

                while (currentbot != zerobot) {
                    if (currentbot.alive == 3) currentbot.step();
                    currentbot = currentbot.next;
                }
                currentbot = currentbot.next;
                generation++;
                if (generation % drawstep == 0) {             // отрисовка на экран через каждые ... шагов
                    paint1();                           // отображаем текущее состояние симуляции на экран
                }
            }
            started = false;        // Закончили работу
        }
    }

    public static World simulation;

    public static void main(String[] args) {
        simulation = new World();
//        simulation.generateMap();
//        simulation.generateAdam();
//        simulation.run();
    }

    // делаем паузу
    // не используется
    /*public void sleep() {
        try {
            int delay = 20;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
    }*/

    // генерируем карту
    public void generateMap(int seed) {
        generation = 0;
        this.map = new int[width][height];
        this.matrix = new Bot[width][height];

        Perlin2D perlin = new Perlin2D(seed);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float f = (float) perlinSlider.getValue();
                float value = perlin.getNoise(x/f,y/f,8,0.45f);        // вычисляем точку ландшафта
                map[x][y] = (int)(value * 255 + 128) & 255;
            }
        }
        mapInGPU = new int[width * height];
        for (int i=0; i<width; i++) {
            for(int j=0; j<height; j++) {
                mapInGPU[j*width+i] = map[i][j];
            }
        }
    }

    // генерируем первого бота
    public void generateAdam() {

        Bot bot = new Bot();
        zerobot.prev = bot;
        zerobot.next = bot;

        bot.adr = 0;            // начальный адрес генома
        bot.x = width / 2;      // координаты бота
        bot.y = height / 2;
        bot.health = 990;       // энергия
        bot.mineral = 0;        // минералы
        bot.alive = 3;          // бот живой
        bot.age = 0;            // возраст
        bot.c_red = 170;        // задаем цвет бота
        bot.c_blue = 170;
        bot.c_green = 170;
        bot.direction = 5;      // направление
        bot.mprev = null;       // бот не входит в многоклеточные цепочки, поэтому ссылки
        bot.mnext = null;       // на предыдущего, следующего в многоклеточной цепочке пусты
        bot.prev = zerobot;     // ссылка на предыдущего
        bot.next = zerobot;     // ссылка на следующего
        for (int i = 0; i < 64; i++) {          // заполняем геном командой 32 - фотосинтез
            bot.mind[i] = 32;
        }

        matrix[bot.x][bot.y] = bot;             // помещаем бота в матрицу
        currentbot = bot;                       // устанавливаем текущим
    }


}
