package com.inso_world.binocular.model.validation

/**
 * Validation group for constraints that should be applied when sending data into the infrastructure layer.
 * Use this for properties that should only be validated before persistence or external system interaction.
 */
interface ToInfrastructure

/**
 * Validation group for constraints that should be applied when receiving data from the infrastructure layer.
 * Use this for properties that should only be validated after loading from persistence or external systems.
 */
interface FromInfrastructure

// The Default group is provided by javax/jakarta.validation and does not need to be redefined here. 