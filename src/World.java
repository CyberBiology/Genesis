import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Основной класс программы.
public class World extends JFrame {

    public int width;
    public int height;
    public int[][] map;    //Карта мира
    public Bot[][] matrix;    //Матрица мира
    public int generation;
    public int population;
    public int organic;

    JPanel paintPanel = new JPanel(new FlowLayout());

    JLabel generationLabel = new JLabel(" Generation: 0 ");
    JLabel populationLabel = new JLabel(" Population: 0 ");
    JLabel organicLabel = new JLabel(" Organic: 0 ");
    Image buffer = null;

    Thread thread = null;
    boolean started = true; // поток работает?
    JPanel canvas = new JPanel() {
    	public void paint(Graphics g) {
    		g.drawImage(buffer, 0, 0, null);
    	}
    };

    JRadioButton baseButton = new JRadioButton("Base", true);
    JRadioButton energyButton = new JRadioButton("Energy", false);
    JRadioButton mineralButton = new JRadioButton("Minerals", false);
    JRadioButton combinedButton = new JRadioButton("Combined", false);
    JRadioButton ageButton = new JRadioButton("Age", false);

    public World() {
    	
        simulation = this;

        setTitle("Genesis 1.0.0");
        setSize(new Dimension(1800, 900));
        Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize(), fSize = getSize();
        if (fSize.height > sSize.height) { fSize.height = sSize.height; }
        if (fSize.width  > sSize.width)  { fSize.width = sSize.width; }
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

        JButton button = new JButton("Generate Map");
        button.addActionListener(new generateMapButtonAction());
        toolbar.add(button);

        JButton startButton = new JButton("Start/Stop");
        startButton.addActionListener(new startButtonAction());
        toolbar.add(startButton);

        ButtonGroup group = new ButtonGroup();
        group.add(baseButton);
        group.add(energyButton);
        group.add(mineralButton);
        group.add(combinedButton);
        group.add(ageButton);
        toolbar.add(baseButton);
        toolbar.add(energyButton);
        toolbar.add(mineralButton);
        toolbar.add(combinedButton);
        toolbar.add(ageButton);

        this.pack();
        this.setVisible(true);
        setExtendedState(MAXIMIZED_BOTH);
    }

    class generateMapButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            width = canvas.getWidth() / 2;    // Ширина доступной части экрана для рисования карты
            height = canvas.getHeight() / 2;
            generateMap((int) (Math.random() * 10000));
            generateAdam();
            paint1();
        }
    }
    class startButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	if(thread==null) {
        		thread	= new Worker(); // создаем новый поток
        		thread.start();
        	} else {
        		started = false;        //Выставляем влаг
        		thread = null;
        	}
        }
    }
    

//    @Override
    public void paint1() {
    	int w = canvas.getWidth();
    	int h = canvas.getHeight();
    	Image buf = canvas.createImage(w, h); //Создаем временный буфер для рисования
    	Graphics g = buf.getGraphics(); //подеменяем графику на временный буфер
//        g.drawRect(0, 0, w, h); // рамка отрисовки

        population = 0;
        organic = 0;
        int mapred;
        int mapgreen;
        int mapblue;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix[x][y] == null) {                     // пустая карта - рисуем голый ланшафт
                    if (map[x][y] < 145) {                      // уровень моря
                        mapred = 5;
                        mapblue = map[x][y] * 3 - 290;
                        mapgreen = map[x][y] * 10 - 1280;;
                        if (mapblue < 20) mapblue = 20;
                        if (mapgreen < 10) mapgreen = 10;
                    } else {
                        mapred = (int)((map[x][y] - 145) * 2.5 + 160);
                        mapgreen = (int)((map[x][y] - 145) * 2.6 + 110);
                        mapblue = (map[x][y] - 145) * 3 + 60;
                        if (mapred > 255) mapred = 255;
                        if (mapblue > 255) mapblue = 255;
                        if (mapgreen > 255) mapgreen = 255;
                    }
                    g.setColor(new Color(mapred, mapgreen, mapblue));
                    g.fillRect(x * 2, y * 2, 2, 2);
                } else if (matrix[x][y].alive == 1) {           // органика, известняк, коралловые рифы
                    if (map[x][y] < 145) {                      // уровень моря
                        mapred = 5;
                        mapblue = map[x][y] * 2 - 120;
                        mapgreen = map[x][y] * 4 - 400;
                        if (mapblue < 40) mapblue = 40;
                        if (mapgreen < 20) mapgreen = 20;
                    } else {                                    // скелетики, трупики на суше
                        mapred = (int)((map[x][y] - 145) * 2.5 + 80);   // уровень моря
                        mapgreen = (int)((map[x][y] - 145) * 2.6 + 60); // уровень моря
                        mapblue = (map[x][y] - 145) * 3 + 30;           // уровень моря
                        if (mapred > 255) mapred = 255;
                        if (mapblue > 255) mapblue = 255;
                        if (mapgreen > 255) mapgreen = 255;
                    }
                    g.setColor(new Color(mapred, mapgreen, mapblue));
                    g.fillRect(x * 2, y * 2, 2, 2);
                    organic = organic + 1;
                } else if (matrix[x][y].alive == 3) {           // живой бот
                    if (baseButton.isSelected()) {
                        g.setColor(new Color(matrix[x][y].c_red, matrix[x][y].c_green, matrix[x][y].c_blue));
                    } else if (energyButton.isSelected()) {
                        mapgreen = 255 - (int)(matrix[x][y].health * 0.25);
                        if (mapgreen < 0) mapgreen = 0;
                        g.setColor(new Color(255, mapgreen, 0));
                    } else if (mineralButton.isSelected()) {
                        mapblue = 255 - (int)(matrix[x][y].mineral * 0.5);
                        if (mapblue < 0) mapblue = 0;
                        g.setColor(new Color(0, 255, mapblue));
                    } else if (combinedButton.isSelected()) {
                        mapgreen = (int) (matrix[x][y].c_green * (1 - matrix[x][y].health * 0.0005));
                        mapblue = (int) (matrix[x][y].c_blue * (0.8 - matrix[x][y].mineral * 0.0005));
                        g.setColor(new Color(matrix[x][y].c_red, mapgreen, mapblue));
                    } else if (ageButton.isSelected()) {
                        mapred = 255 - (int)(Math.sqrt(matrix[x][y].age) * 4);
                        if (mapred < 0) mapred = 0;
                        g.setColor(new Color(mapred, 0, 255));
                    }
                    g.fillRect(x * 2, y * 2, 2, 2);
                    population = population + 1;
                }
            }
        }

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
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (matrix[x][y] != null) {
                            if (matrix[x][y].alive == 3) {
                                matrix[x][y].step();        // выполняем шаг бота
                            }
                        }
                    }
                }

                generation = generation + 1;
                if (generation % 10 == 0) {             // отрисовка на экран через каждые ... шагов
                    paint1();                           // отображаем текущее состояние симуляции на экран
                }
//	            sleep();                                // пауза между ходами, если надо уменьшить скорость
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
                float value = perlin.getNoise(x/160f,y/160f,8,0.45f);        // вычисляем точку ландшафта
                map[x][y] = (int)(value * 255 + 128) & 255;
            }
        }
    }

    // генерируем первого бота
    public void generateAdam() {
        Bot bot = new Bot();

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
        for (int i = 0; i < 64; i++) {          // заполняем геном командой 25 - фотосинтез
            bot.mind[i] = 25;
        }

        matrix[bot.x][bot.y] = bot;             // помещаем бота в матрицу
    }


}
