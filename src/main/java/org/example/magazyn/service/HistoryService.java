package org.example.magazyn.service;

import org.example.magazyn.entity.History;

import java.util.List;

public interface HistoryService {

    History addHistoryEntry(Long userId, String operation, String details);

    List<History> getAllHistory();

    List<History> getUserHistory(Long userId);

    History logProductReservation(Long userId, Long productId, String productName, int quantity);

    History logReservationCancellation(Long userId, Long reservationId, String productName, int quantity);

    History logReservationStatusChange(Long userId, Long reservationId, String oldStatus, String newStatus, String productName);

    History logZoneCreation(Long userId, Long zoneId, String zoneName, double maxCapacity);

    History logZoneUpdate(Long userId, Long zoneId, String oldName, String newName,
                          double oldCapacity, double newCapacity);

    History logZoneDeletion(Long userId, Long zoneId, String zoneName);

    History logProductZoneAssignment(Long userId, Long productId, String productName,
                                     Long zoneId, String zoneName, int quantity);

    History logProductZoneRemoval(Long userId, Long productId, String productName,
                                  Long zoneId, String zoneName);

    History logUserRoleChange(Long adminUserId, Long targetUserId, String targetUserEmail,
                              String oldRole, String newRole);
}