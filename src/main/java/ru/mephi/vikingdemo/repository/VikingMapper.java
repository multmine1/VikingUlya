package ru.mephi.vikingdemo.repository;

import org.springframework.stereotype.Component;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.EquipmentItemEntity;
import ru.mephi.vikingdemo.model.StoredViking;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.model.VikingEntity;

import java.util.List;

@Component
public class VikingMapper {

    public VikingEntity toVikingEntity(Viking viking) {
        return toVikingEntity(null, viking);
    }

    public VikingEntity toVikingEntity(Integer id, Viking viking) {
        return new VikingEntity(
                id,
                viking.name(),
                viking.age(),
                viking.heightCm(),
                viking.hairColor(),
                viking.beardStyle(),
                ""
        );
    }

    public EquipmentItemEntity toEquipmentItemEntity(Integer vikingId, EquipmentItem item) {
        return new EquipmentItemEntity(
                null,
                vikingId,
                item.name(),
                item.quality()
        );
    }

    public EquipmentItem toEquipmentItem(EquipmentItemEntity entity) {
        return new EquipmentItem(
                entity.name(),
                entity.quality()
        );
    }

    public Viking toViking(VikingEntity entity, List<EquipmentItemEntity> equipmentEntities) {
        List<EquipmentItem> equipment = equipmentEntities.stream()
                .map(this::toEquipmentItem)
                .toList();

        return new Viking(
                entity.name(),
                entity.age(),
                entity.heightCm(),
                entity.hairColor(),
                entity.beardStyle(),
                equipment
        );
    }

    public StoredViking toStoredViking(VikingEntity entity, List<EquipmentItemEntity> equipmentEntities) {
        return new StoredViking(
                entity.id(),
                toViking(entity, equipmentEntities)
        );
    }
}
