package com.andikas.pantaubumi.data.mapper

import com.andikas.pantaubumi.data.remote.model.AlertDto
import com.andikas.pantaubumi.data.remote.model.AlertsResponseDto
import com.andikas.pantaubumi.data.remote.model.EvacuationDto
import com.andikas.pantaubumi.data.remote.model.FlagReportResponse
import com.andikas.pantaubumi.data.remote.model.GeometryDto
import com.andikas.pantaubumi.data.remote.model.ReportDto
import com.andikas.pantaubumi.data.remote.model.RiskDto
import com.andikas.pantaubumi.data.remote.model.RiskFeatureDto
import com.andikas.pantaubumi.data.remote.model.RiskZoneDto
import com.andikas.pantaubumi.data.remote.model.RouteDto
import com.andikas.pantaubumi.data.remote.model.RouteStepDto
import com.andikas.pantaubumi.domain.model.Alert
import com.andikas.pantaubumi.domain.model.AlertsResponse
import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.model.FlagReportResult
import com.andikas.pantaubumi.domain.model.Geometry
import com.andikas.pantaubumi.domain.model.Report
import com.andikas.pantaubumi.domain.model.Risk
import com.andikas.pantaubumi.domain.model.RiskFeature
import com.andikas.pantaubumi.domain.model.RiskZone
import com.andikas.pantaubumi.domain.model.Route
import com.andikas.pantaubumi.domain.model.RouteStep

fun RiskDto.toDomain(): Risk {
    return Risk(
        lat = lat,
        lng = lng,
        floodScore = floodScore,
        landslideScore = landslideScore,
        earthquakeScore = earthquakeScore,
        overallRisk = overallRisk,
        computedAt = computedAt
    )
}

fun AlertDto.toDomain(): Alert {
    return Alert(
        id = id,
        type = type,
        lat = lat,
        lng = lng,
        severity = severity,
        message = message,
        source = source,
        createdAt = createdAt
    )
}

fun AlertsResponseDto.toDomain(): AlertsResponse {
    return AlertsResponse(
        items = items.map { it.toDomain() },
        nextCursor = nextCursor,
        hasMore = hasMore
    )
}

fun EvacuationDto.toDomain(): Evacuation {
    return Evacuation(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        capacity = capacity,
        type = type,
        address = address,
        distanceKm = distanceKm
    )
}

fun ReportDto.toDomain(): Report {
    return Report(
        id = id,
        lat = lat,
        lng = lng,
        text = text,
        category = category,
        verified = verified,
        verificationScore = verificationScore,
        source = source,
        flagCount = flagCount,
        createdAt = createdAt
    )
}

fun RiskZoneDto.toDomain(): RiskZone {
    return RiskZone(
        type = type,
        features = features.map { it.toDomain() }
    )
}

fun RiskFeatureDto.toDomain(): RiskFeature {
    return RiskFeature(
        type = type,
        riskLevel = properties.riskLevel,
        score = properties.score,
        geometry = geometry.toDomain()
    )
}

fun GeometryDto.toDomain(): Geometry {
    return Geometry(
        type = type,
        coordinates = coordinates
    )
}

fun RouteDto.toDomain(): Route {
    return Route(
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        geometry = geometry,
        steps = steps.map { it.toDomain() }
    )
}

fun RouteStepDto.toDomain(): RouteStep {
    return RouteStep(
        instruction = instruction,
        distanceMeters = distanceMeters,
        location = location
    )
}

fun FlagReportResponse.toDomain(): FlagReportResult {
    return FlagReportResult(
        reportId = reportId,
        flagCount = flagCount,
        hidden = hidden
    )
}
