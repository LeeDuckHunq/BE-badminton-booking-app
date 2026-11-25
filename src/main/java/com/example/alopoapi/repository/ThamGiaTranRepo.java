package com.example.alopoapi.repository;

import com.example.alopoapi.entity.nhom.ThamGiaTranEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ThamGiaTranRepo extends MongoRepository<ThamGiaTranEntity, String> {

    List<ThamGiaTranEntity> findByMaTran(String maTran);

    List<ThamGiaTranEntity> findByMaTranIn(Collection<String> maTran);

    List<ThamGiaTranEntity> findByUsername(String username);

    List<ThamGiaTranEntity> findByUsernameAndTrangThai(String username, String trangThai);
}
