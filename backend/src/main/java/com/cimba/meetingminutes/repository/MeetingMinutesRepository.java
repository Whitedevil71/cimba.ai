package com.cimba.meetingminutes.repository;

import com.cimba.meetingminutes.model.MeetingMinutes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingMinutesRepository extends JpaRepository<MeetingMinutes, Long> {
}
