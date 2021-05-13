package org.powbot.krulvis.miner;

import org.jetbrains.annotations.NotNull;
import org.powbot.krulvis.api.ATContext;
import org.powbot.krulvis.api.gui.ATGUI;
import org.powbot.krulvis.api.utils.resources.ATGson;
import org.powerbot.script.Tile;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MinerGUI extends ATGUI<Miner, MinerProfile> {
    private JPanel rootPanel;
    private JList rockLocationList = new JList();
    private JPanel minerPanel;
    private JScrollPane tableScrollPane;
    private JTextField centerTileField;
    private JButton currentPositionButton;
    private JCheckBox dropOresCheckBox;
    private JSlider radiusSlider;
    private JCheckBox fastMiningCheckBox;
    public ArrayList<Tile> oreLocations = new ArrayList<>();

    public MinerGUI(@NotNull Miner script) {
        super(script);
        System.out.println("Miner: " + script);
        rockLocationList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JList list = (JList) e.getSource();
                int index = list.getSelectedIndex();
                if (e.getClickCount() == 2 && index != -1) {
                    removeRock(index);
                }
            }
        });

        currentPositionButton.addActionListener(e -> {
            centerTileField.setText(ATContext.INSTANCE.getMe().tile().toString());
        });

        tableScrollPane.setViewportView(rockLocationList);
        initialize(rootPanel);
        setSize(new Dimension(330, 360));
    }

    Tile getCenterTile() {
        String ts = centerTileField.getText().replaceAll("[^0-9,]", "");
        String[] parts = ts.split(",");
//        ATContext.INSTANCE.debug("CenterTileString: " + ts);
//        for (String part :
//                parts) {
//            ATContext.INSTANCE.debug("part: " + part);
//        }
        if (parts.length == 3) {
            Tile t = new Tile(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            return t;
        } else {
            return null;
        }
    }

    int getRadius() {
        return radiusSlider.getValue();
    }


    void resetRockListModel() {
        DefaultListModel<Tile> model = new DefaultListModel();
        model.addAll(oreLocations);
        rockLocationList.setModel(model);
    }

    void removeRock(int row) {
        oreLocations.remove(row);
        resetRockListModel();
    }

    void addRock(Tile oreLocation) {
        if (!oreLocations.contains(oreLocation)) {
            oreLocations.add(oreLocation);
            resetRockListModel();
        }
    }

    @Override
    public void onStart(@NotNull Miner script, @NotNull MinerProfile settings) {
        script.setProfile(settings);
    }

    @NotNull
    @Override
    public MinerProfile getCurrentSettings() {
        return new MinerProfile(getCenterTile(), getRadius(), dropOresCheckBox.isSelected(), fastMiningCheckBox.isSelected(), oreLocations);
    }

    @Override
    public void loadSettings(@NotNull MinerProfile settings) {
        oreLocations.clear();
        oreLocations.addAll(settings.getOreLocations());
        resetRockListModel();
        centerTileField.setText(settings.getCenter().toString());
        radiusSlider.setValue(settings.getRadius());
        dropOresCheckBox.setSelected(settings.getDropOres());
        fastMiningCheckBox.setSelected(settings.getFastMining());
    }

    @NotNull
    @Override
    public MinerProfile parseSettings(@NotNull String inputText) {
        return ATGson.INSTANCE.Gson().fromJson(inputText, MinerProfile.class);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridBagLayout());
        minerPanel = new JPanel();
        minerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(minerPanel, gbc);
        minerPanel.setBorder(BorderFactory.createTitledBorder(null, "Miner Profile", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tableScrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 100;
        gbc.ipady = 50;
        minerPanel.add(tableScrollPane, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Center:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        minerPanel.add(label1, gbc);
        centerTileField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 30.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        minerPanel.add(centerTileField, gbc);
        currentPositionButton = new JButton();
        currentPositionButton.setText("Current Position");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        minerPanel.add(currentPositionButton, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        minerPanel.add(spacer1, gbc);
        dropOresCheckBox = new JCheckBox();
        dropOresCheckBox.setText("Drop Ores");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        minerPanel.add(dropOresCheckBox, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Radius:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        minerPanel.add(label2, gbc);
        radiusSlider = new JSlider();
        radiusSlider.setMaximum(25);
        radiusSlider.setMinimum(1);
        radiusSlider.setMinorTickSpacing(1);
        radiusSlider.setPaintLabels(true);
        radiusSlider.setPaintTicks(true);
        radiusSlider.setSnapToTicks(false);
        radiusSlider.setValue(5);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        minerPanel.add(radiusSlider, gbc);
        fastMiningCheckBox = new JCheckBox();
        fastMiningCheckBox.setText("Fast Mining");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        minerPanel.add(fastMiningCheckBox, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
