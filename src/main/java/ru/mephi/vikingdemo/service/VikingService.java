package ru.mephi.vikingdemo.service;

import org.springframework.stereotype.Service;
import ru.mephi.vikingdemo.model.StoredViking;
import ru.mephi.vikingdemo.model.Viking;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import ru.mephi.vikingdemo.repository.VikingStorage;

@Service
public class VikingService {
    // каждый раз при изменении создаётся новая копия списка 

    private final VikingFactory vikingFactory;
    private final VikingStorage vikingStorage;
    
    
    @Autowired
    public VikingService(
            VikingFactory vikingFactory,
            VikingStorage vikingStorage
    ) {
        this.vikingFactory = vikingFactory;
        this.vikingStorage = vikingStorage;
    }
    
    public List<StoredViking> findAll() {
        return vikingStorage.findAll();
    }

    public StoredViking createRandomViking() {
        Viking viking = vikingFactory.createRandomViking();
        return vikingStorage.save(viking);
    }

    public StoredViking create(Viking viking) {
        return vikingStorage.save(viking);
    }

    public boolean deleteById(int id) {
        return vikingStorage.deleteById(id);
    }

    public Optional<StoredViking> updateById(int id, Viking viking) {
        return vikingStorage.updateById(id, viking);
    }

    public List<StoredViking> generateRandomVikings(int count) {
        if (count <= 0) {
            return List.of();
        }

        return IntStream.range(0, count)
                .mapToObj(index -> vikingFactory.createRandomViking())
                .map(vikingStorage::save)
                .toList();
    }
}
