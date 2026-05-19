package ru.mephi.vikingdemo.gui;

import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.StoredViking;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.service.VikingLambdaService;
import ru.mephi.vikingdemo.service.VikingService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class VikingDesktopFrame extends JFrame {

    private final VikingService vikingService;
    private final VikingLambdaService vikingLambdaService;
    private final VikingTableModel tableModel = new VikingTableModel();

    public VikingDesktopFrame(VikingService vikingService, VikingLambdaService vikingLambdaService) {
        this.vikingService = vikingService;
        this.vikingLambdaService = vikingLambdaService;

        setTitle("Viking Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1000, 420));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel header = new JLabel("Viking Demo", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        add(header, BorderLayout.NORTH);

        JTable vikingTable = new JTable(tableModel);
        vikingTable.setRowHeight(28);
        add(new JScrollPane(vikingTable), BorderLayout.CENTER);

        JButton createButton = new JButton("Create random viking");
        createButton.addActionListener(event -> onCreateViking());
        JButton addButton = new JButton("Add viking");
        addButton.addActionListener(event -> onAddViking());
        JButton deleteButton = new JButton("Delete by id");
        deleteButton.addActionListener(event -> onDeleteById());
        JButton updateButton = new JButton("Update by id");
        updateButton.addActionListener(event -> onUpdateById());
        JButton lambdaButton = new JButton("Lambda tools");
        lambdaButton.addActionListener(event -> onOpenLambdaTools());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(createButton);
        bottomPanel.add(addButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(updateButton);
        bottomPanel.add(lambdaButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        onInit();
    }

    private void onCreateViking() {
        StoredViking viking = vikingService.createRandomViking();
        tableModel.addViking(viking);
    }

    private void onAddViking() {
        Viking viking = inputViking();
        if (viking == null) {
            return;
        }

        vikingService.create(viking);
        refreshTable();
    }

    private void onDeleteById() {
        Integer id = inputId();
        if (id == null) {
            return;
        }

        boolean deleted = vikingService.deleteById(id);
        if (!deleted) {
            showMessage("Viking not found");
        }
        refreshTable();
    }

    private void onUpdateById() {
        Integer id = inputId();
        if (id == null) {
            return;
        }

        Viking currentViking = findVikingById(id);
        if (currentViking == null) {
            showMessage("Viking not found");
            return;
        }

        Viking viking = inputViking(currentViking);
        if (viking == null) {
            return;
        }

        if (vikingService.updateById(id, viking).isEmpty()) {
            showMessage("Viking not found");
        }
        refreshTable();
    }

    private void onOpenLambdaTools() {
        new VikingLambdaPanelFrame(vikingLambdaService, vikingService, this::refreshTable).setVisible(true);
    }
    
    public void addNewViking(StoredViking viking){
        tableModel.addViking(viking);
    }

    private void onInit() {
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setVikings(vikingService.findAll());
    }

    private Integer inputId() {
        String value = JOptionPane.showInputDialog(this, "Viking id:");
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            showMessage("Invalid id");
            return null;
        }
    }

    private Viking inputViking() {
        return inputViking(null);
    }

    private Viking inputViking(Viking defaults) {
        String name = inputText("Name:", defaults == null ? "" : defaults.name());
        if (name == null || name.isBlank()) {
            return null;
        }

        Integer age = inputPositiveInt("Age:", defaults == null ? null : defaults.age());
        if (age == null) {
            return null;
        }

        Integer height = inputPositiveInt("Height (cm):", defaults == null ? null : defaults.heightCm());
        if (height == null) {
            return null;
        }

        HairColor hairColor = (HairColor) JOptionPane.showInputDialog(
                this,
                "Hair color:",
                "Hair color",
                JOptionPane.QUESTION_MESSAGE,
                null,
                HairColor.values(),
                defaults == null ? HairColor.Blond : defaults.hairColor()
        );
        if (hairColor == null) {
            return null;
        }

        BeardStyle beardStyle = (BeardStyle) JOptionPane.showInputDialog(
                this,
                "Beard style:",
                "Beard style",
                JOptionPane.QUESTION_MESSAGE,
                null,
                BeardStyle.values(),
                defaults == null ? BeardStyle.SHORT : defaults.beardStyle()
        );
        if (beardStyle == null) {
            return null;
        }

        List<EquipmentItem> equipment = inputEquipment(defaults == null ? List.of() : defaults.equipment());
        if (equipment == null) {
            return null;
        }

        return new Viking(
                name.trim(),
                age,
                height,
                hairColor,
                beardStyle,
                equipment
        );
    }

    private Integer inputPositiveInt(String message, Integer defaultValue) {
        String value = defaultValue == null
                ? JOptionPane.showInputDialog(this, message)
                : JOptionPane.showInputDialog(this, message, defaultValue);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            int number = Integer.parseInt(value.trim());
            if (number <= 0) {
                showMessage("Value must be positive");
                return null;
            }
            return number;
        } catch (NumberFormatException exception) {
            showMessage("Invalid number");
            return null;
        }
    }

    private List<EquipmentItem> inputEquipment() {
        return inputEquipment(List.of());
    }

    private List<EquipmentItem> inputEquipment(List<EquipmentItem> defaultEquipment) {
        String value = JOptionPane.showInputDialog(
                this,
                "Equipment as name:quality; name:quality",
                formatEquipmentInput(defaultEquipment)
        );
        if (value == null) {
            return null;
        }

        try {
            return parseEquipment(value);
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
            return null;
        }
    }

    private List<EquipmentItem> parseEquipment(String value) {
        List<EquipmentItem> equipment = new ArrayList<>();
        if (value.isBlank()) {
            return equipment;
        }

        String[] items = value.split(";");
        for (String item : items) {
            String trimmedItem = item.trim();
            if (trimmedItem.isEmpty()) {
                continue;
            }

            String[] parts = trimmedItem.split(":", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IllegalArgumentException("Equipment format: name:quality; name:quality");
            }

            equipment.add(new EquipmentItem(parts[0].trim(), parts[1].trim()));
        }

        return equipment;
    }

    private String inputText(String message, String defaultValue) {
        return JOptionPane.showInputDialog(this, message, defaultValue);
    }

    private Viking findVikingById(int id) {
        return vikingService.findAll().stream()
                .filter(storedViking -> Objects.equals(storedViking.id(), id))
                .map(StoredViking::viking)
                .findFirst()
                .orElse(null);
    }

    private String formatEquipmentInput(List<EquipmentItem> equipment) {
        if (equipment == null || equipment.isEmpty()) {
            return "Axe:Rare; Shield:Common";
        }

        StringBuilder result = new StringBuilder();
        for (EquipmentItem item : equipment) {
            if (!result.isEmpty()) {
                result.append("; ");
            }
            result.append(item.name()).append(":").append(item.quality());
        }

        return result.toString();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
