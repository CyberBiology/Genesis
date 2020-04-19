import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame {

    Image buffer = null;

    JPanel canvas = new JPanel() {
        public void paint(Graphics g) {
            g.drawImage(buffer, 0, 0, null);
        }
    };

    JPanel paintPanel = new JPanel(new FlowLayout());
    JLabel generationLabel = new JLabel(" Generation: 0 ");
    JLabel populationLabel = new JLabel(" Population: 0 ");
    JLabel organicLabel = new JLabel(" Organic: 0 ");



    JRadioButton baseButton = new JRadioButton("Base", true);
    JRadioButton combinedButton = new JRadioButton("Combined", false);
    JRadioButton energyButton = new JRadioButton("Energy", false);
    JRadioButton mineralButton = new JRadioButton("Minerals", false);
    JRadioButton ageButton = new JRadioButton("Age", false);
    JRadioButton familyButton = new JRadioButton("Family", false);

    JSlider perlinSlider = new JSlider (JSlider.HORIZONTAL, 0, 480, 300);
    JButton mapButton = new JButton("Create Map");
    JSlider sealevelSlider = new JSlider (JSlider.HORIZONTAL, 0, 256, 145);
    JButton startButton = new JButton("Start/Stop");
    JSlider drawstepSlider = new JSlider (JSlider.HORIZONTAL, 0, 40, 10);

    public Gui() {
    }

    public void init() {
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

        //mapButton.addActionListener(new World.mapButtonAction());
        toolbar.add(mapButton);

        JLabel slider2Label = new JLabel("Sea level");
        toolbar.add(slider2Label);

        //sealevelSlider.addChangeListener(new World.sealevelSliderChange());
        sealevelSlider.setMajorTickSpacing(128);
        sealevelSlider.setMinorTickSpacing(64);
        sealevelSlider.setPaintTicks(true);
        sealevelSlider.setPaintLabels(true);
        sealevelSlider.setPreferredSize(new Dimension(100, sealevelSlider.getPreferredSize().height));
        sealevelSlider.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        toolbar.add(sealevelSlider);

        //startButton.addActionListener(new World.startButtonAction());
        toolbar.add(startButton);

        JLabel slider3Label = new JLabel("Draw step");
        toolbar.add(slider3Label);

        //drawstepSlider.addChangeListener(new World.drawstepSliderChange());
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
        group.add(familyButton);
        toolbar.add(baseButton);
        toolbar.add(combinedButton);
        toolbar.add(energyButton);
        toolbar.add(mineralButton);
        toolbar.add(ageButton);
        toolbar.add(familyButton);

        this.pack();
        this.setVisible(true);
        setExtendedState(MAXIMIZED_BOTH);
    }
}
