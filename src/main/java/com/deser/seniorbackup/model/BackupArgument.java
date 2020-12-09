package com.deser.seniorbackup.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonTypeInfo(use = CLASS, property = "type")
public abstract class BackupArgument {
    private Map<Long, PlayerInventoryArgument> backups;
}
