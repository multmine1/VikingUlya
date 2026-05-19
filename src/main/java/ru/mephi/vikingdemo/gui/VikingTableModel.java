package ru.mephi.vikingdemo.gui;

import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.StoredViking;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VikingTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Name", "Age", "Height (cm)", "Hair color", "Beard style", "Equipment"};
    private final List<StoredViking> data = new ArrayList<>();

    public void addViking(StoredViking viking) {
        int row = data.size();
        data.add(viking);
        fireTableRowsInserted(row, row);
    }

    public void setVikings(List<StoredViking> vikings) {
        data.clear();
        data.addAll(vikings);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        StoredViking storedViking = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> storedViking.id();
            case 1 -> storedViking.name();
            case 2 -> storedViking.age();
            case 3 -> storedViking.heightCm();
            case 4 -> storedViking.hairColor();
            case 5 -> storedViking.beardStyle();
            case 6 -> formatEquipment(storedViking.equipment());
            default -> "";
        };
    }

    private String formatEquipment(List<EquipmentItem> equipment) {
        if (equipment == null) {
            return "";
        }

        return equipment.stream()
                .map(item -> item.name() + " [" + item.quality() + "]")
                .collect(Collectors.joining(", "));
    }
}
