package ru.mephi.vikingdemo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.EquipmentItemEntity;
import ru.mephi.vikingdemo.model.StoredViking;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.model.VikingEntity;


@Repository
public class VikingStorage {

    private final VikingRepository vikingRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final VikingMapper vikingMapper;

    public VikingStorage(
            VikingRepository vikingRepository,
            EquipmentItemRepository equipmentItemRepository,
            VikingMapper vikingMapper
    ) {
        this.vikingRepository = vikingRepository;
        this.equipmentItemRepository = equipmentItemRepository;
        this.vikingMapper = vikingMapper;
    }

    @Transactional
    public StoredViking save(Viking viking) {
        Integer vikingId = vikingRepository.save(
                vikingMapper.toVikingEntity(viking)
        );

        Viking normalizedViking = normalizeViking(viking);
        for (EquipmentItem item : normalizedViking.equipment()) {
            equipmentItemRepository.save(
                    vikingMapper.toEquipmentItemEntity(vikingId, item)
            );
        }

        return new StoredViking(vikingId, normalizedViking);
    }

    public List<StoredViking> findAll() {
        List<VikingEntity> vikingEntities = vikingRepository.findAll();
        List<EquipmentItemEntity> equipmentEntities = equipmentItemRepository.findAll();

        Map<Integer, List<EquipmentItemEntity>> equipmentByVikingId = equipmentEntities.stream()
                .collect(Collectors.groupingBy(EquipmentItemEntity::vikingId));

        return vikingEntities.stream()
                .map(vikingEntity -> vikingMapper.toStoredViking(
                        vikingEntity,
                        equipmentByVikingId.getOrDefault(vikingEntity.id(), List.of())
                ))
                .toList();
    }

    @Transactional
    public boolean deleteById(int id) {
        return vikingRepository.deleteById(id);
    }

    @Transactional
    public Optional<StoredViking> updateById(int id, Viking viking) {
        if (vikingRepository.findById(id).isEmpty()) {
            return Optional.empty();
        }

        Viking normalizedViking = normalizeViking(viking);
        VikingEntity entity = vikingMapper.toVikingEntity(id, normalizedViking);

        if (!vikingRepository.update(entity)) {
            return Optional.empty();
        }

        equipmentItemRepository.deleteByVikingId(id);
        for (EquipmentItem item : normalizedViking.equipment()) {
            equipmentItemRepository.save(
                    vikingMapper.toEquipmentItemEntity(id, item)
            );
        }

        return Optional.of(new StoredViking(id, normalizedViking));
    }

    private Viking normalizeViking(Viking viking) {
        if (viking.equipment() != null) {
            return viking;
        }

        return new Viking(
                viking.name(),
                viking.age(),
                viking.heightCm(),
                viking.hairColor(),
                viking.beardStyle(),
                List.of()
        );
    }
}
