package com.digitopia.casestudy.organization

data class CreateOrganizationRequest(
    val organizationName: String,
    val registryNumber: String,
    val contactEmail: String,
    val companySize: Int,
    val yearFounded: Int
)

data class UpdateOrganizationRequest(
    val organizationName: String,
    val contactEmail: String,
    val companySize: Int
)