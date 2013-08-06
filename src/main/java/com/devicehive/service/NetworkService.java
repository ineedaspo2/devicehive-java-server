package com.devicehive.service;

import com.devicehive.dao.NetworkDAO;
import com.devicehive.exceptions.HiveException;
import com.devicehive.model.Network;
import com.devicehive.model.NullableWrapper;
import com.devicehive.model.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Stateless
public class NetworkService {


    @Inject
    private NetworkDAO networkDAO;

    public Network getById(long id) {
        return networkDAO.getById(id);
    }

    public Network getWithDevicesAndDeviceClasses(long id, User u) {
        if (u.isAdmin()) {
            return networkDAO.getWithDevicesAndDeviceClasses(id);
        } else {
            return networkDAO.getWithDevicesAndDeviceClasses(id, u.getId());
        }

    }

    public boolean delete(long id) {
        return networkDAO.delete(id);
    }

    public Network insert(Network n) {
        if (n.getName() == null) {
            throw new HiveException("Name must be provided");
        }
        return networkDAO.createNetwork(n);
    }

    public Network update(Network n) {

        if (n.getId() == null) {
            throw new HiveException("Id must pe provided");
        }

        Network updated = networkDAO.getById(n.getId());

        if (n.getKey() != null) {
            updated.setKey(n.getKey());
        }

        if (n.getDescription() != null) {
            updated.setDescription(n.getDescription());
        }

        if (n.getName() != null) {
            updated.setName(n.getName());
        }

        if (networkDAO.update(n.getId(), n)) {
            return n;
        }
        return null;

    }

    public List<Network> list(String name, String namePattern,
                              String sortField, boolean sortOrder,
                              Integer take, Integer skip, Long userId) {
        return networkDAO.list(name, namePattern, sortField, sortOrder, take, skip, userId);
    }

    public Network createOrVeriryNetwork(NullableWrapper<Network> network, UUID deviceGuid) {
        Network stored;
        //case network is not defined
        if (network == null) {
            return networkDAO.findByDevice(deviceGuid);
        }
        if (network.getValue() != null) {
            Network update = network.getValue();
            if (update.getId() != null) {
                stored = networkDAO.getById(update.getId());
            } else {
                stored = networkDAO.findByName(update.getName());
            }
            if (stored != null) {
                if (stored.getKey() != null) {
                    if (!stored.getKey().equals(update.getKey())) {
                        throw new HiveException("Wrong network key!", 403);
                    }
                }
            } else {
                if (update.getId() != null){
                    throw new HiveException("Invalid request");
                }
                stored = networkDAO.createNetwork(update);
            }
            assert (stored != null);
            return stored;
        }
        return null;
    }
}
