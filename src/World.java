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
    public int[][] map;    //Матрица мира
    public Bot[][] matrix;    //Матрица мира
    public int generation;
    public int population;
    public int organic;

    JPanel paintPanel = new JPanel(new FlowLayout());

    JLabel generationLabel = new JLabel(" Generation: 0 ");
    JLabel populationLabel = new JLabel(" Population: 0 ");
    JLabel organicLabel = new JLabel(" Organic: 0 ");
    Image buffer	= null;
    boolean runing	= false;
    JButton startButton = new JButton("Start/Stop");
    
    Thread thread = null;
    boolean started=true; // поток работает?
    JPanel canvas = new JPanel()
    {
    	public void paint(Graphics g)
    	{
    		g.drawImage(buffer, 0, 0, null);
    	};
    }; 
    
    public World() {
    	
    	population	= 0;
    	
        
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
//        button.setSize(100,30);
        button.addActionListener(new generateMapButtonAction());
        toolbar.add(button);


//        button.setSize(100,30);
        startButton.addActionListener(new startButtonAction());
        toolbar.add(startButton);

        //paintPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        //paintPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        //container.add(paintPanel, BorderLayout.CENTER);

        this.pack();
        this.setVisible(true);
        setExtendedState(MAXIMIZED_BOTH);
    }

    class generateMapButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	//System.out.println("width="+canvas.getWidth()+" height="+canvas.getHeight());
        	
        	if(map==null)// если карту еще не генерили, тода создаем
        	{
	        	width = canvas.getWidth()/2;// Ширина доступной части экрана для рисования карты
	        	height = canvas.getHeight()/2;// Боты 4 пикселя?
	        	
	            generateMap((int) (Math.random() * 10000));
	            generateAdam();
	            paint1();
        	}
        }
    }
    class startButtonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	if(thread==null)
        	{
        		thread	= new Worker();// создаем новый поток
        		thread.start();//Запускаем его
        	}else
        	{
        		started = false;//Выставляем влаг 
        		thread = null;
        	}
        }
    }
    

//    @Override
    public void paint1() {
    	int leftout = 0;// Раньше был отступ справа
    	int top = 0;// раньше был отступ сверху
    	
    	int w = canvas.getWidth();
    	int h = canvas.getHeight();
    	//Создаем временный буфер для рисования
    	Image buf = canvas.createImage(w, h);
    	//подеменяем графику на временный буфер
    	Graphics g = buf.getGraphics();

                                              // отступ отрисовки
        g.drawRect(0, 0, w, h); // рамка отрисовки

        population = 0;
        organic = 0;
        int mapred;
        int mapgreen;
        int mapblue;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix[x][y] == null) {                     // пустая карта
                    if (map[x][y] < 145) {                      // рисуем голый ланшафт
                        mapred = 5;
                        mapblue = map[x][y] * 3 - 290;
                        mapgreen = map[x][y] * 10 - 1280;;
                        if (mapblue < 20) {mapblue = 20;}
                        if (mapgreen < 10) {mapgreen = 10;}
                    } else {
                        mapred = (int)((map[x][y] - 145) * 2.5 + 160);
                        mapgreen = (int)((map[x][y] - 145) * 2.6 + 110);
                        mapblue = (int)((map[x][y] - 145) * 3 + 60);
                        if (mapred > 255) {mapred = 255;}
                        if (mapblue > 255) {mapblue = 255;}
                        if (mapgreen > 255) {mapgreen = 255;}
                    }
                    g.setColor(new Color(mapred, mapgreen, mapblue));
                    g.fillRect(leftout + x * 2, top + y * 2, 2, 2);
                } else if (matrix[x][y].alive == 1) {           // органика
                    if (map[x][y] < 145) {                      // известняк, коралловые рифы
                        mapred = 5;
                        mapblue = map[x][y] * 2 - 120;
                        mapgreen = map[x][y] * 4 - 400;
                        if (mapblue < 40) {mapblue = 40;}
                        if (mapgreen < 20) {mapgreen = 20;}
                    } else {                                    // скелетики, трупики на суше
                        mapred = (int)((map[x][y] - 145) * 2.5 + 80);
                        mapgreen = (int)((map[x][y] - 145) * 2.6 + 60);
                        mapblue = (int)((map[x][y] - 145) * 3 + 30);
                        if (mapred > 255) {mapred = 255;}
                        if (mapblue > 255) {mapblue = 255;}
                        if (mapgreen > 255) {mapgreen = 255;}
                    }
                    g.setColor(new Color(mapred, mapgreen, mapblue));
                    g.fillRect(leftout + x * 2, top + y * 2, 2, 2);
                    organic = organic + 1;
                } else if (matrix[x][y].alive == 3) {           // живой бот
//                    g.setColor(Color.BLACK);
//                    g.drawRect(leftout + x * 2, 50 + y * 2, 2, 2);
//                    g.setColor(new Color(matrix[x][y].c_red, matrix[x][y].c_green, matrix[x][y].c_blue));
                    mapgreen = (int) (matrix[x][y].c_green - ((matrix[x][y].c_green * matrix[x][y].health) * 0.0005));
                    if (mapgreen < 0) mapgreen = 0;
                    if (mapgreen > 255) mapgreen = 255;
                    mapblue = (int) (matrix[x][y].c_blue * 0.8 - ((matrix[x][y].c_blue * matrix[x][y].mineral) * 0.0005));
                    mapred = matrix[x][y].c_red;

                    g.setColor(new Color(mapred, mapgreen, mapblue));
                    g.fillRect(leftout + x * 2, top + y * 2, 2, 2);
                    population = population + 1;
                }
            }
        }

        generationLabel.setText(" Generation: " + String.valueOf(generation));
        populationLabel.setText(" Population: " + String.valueOf(population));
        organicLabel.setText(" Organic: " + String.valueOf(organic));
        
        buffer = buf;
        canvas.repaint();
        //canvas.getGraphics().drawImage(buf, 0, 0, null);
    }

    // Основной цикл ----------------------------------------------------------------------------------
    // Теперь не используется, переехал в Thread
/*    public void run() {
        //пока не остановят симуляцию
        generation = 0;

        /*while (true) {
            // обновляем матрицу
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
                paint1(getGraphics());              // отображаем текущее состояние симуляции на экран
            }
//            sleep();                                // пауза между ходами, если надо уменьшить скорость
        }
    }*/
   class Worker extends Thread
   {
		public void run()
		{
	        started	= true;// Флаг работы потока, если установить в false  поток заканчивает работу
	        while (started) {
	            // обновляем матрицу
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
	                paint1();              // отображаем текущее состояние симуляции на экран
	            }
//	            sleep();                                // пауза между ходами, если надо уменьшить скорость
	        }
	        started = false;// Закончили работу
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
