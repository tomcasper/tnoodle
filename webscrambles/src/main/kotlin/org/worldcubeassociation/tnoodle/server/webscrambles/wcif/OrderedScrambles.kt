package org.worldcubeassociation.tnoodle.server.webscrambles.wcif

import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import org.worldcubeassociation.tnoodle.server.webscrambles.ScrambleRequest
import org.worldcubeassociation.tnoodle.server.webscrambles.wcif.WCIFParser.atLocalStartOfDay
import org.worldcubeassociation.tnoodle.server.webscrambles.ScrambleRequest.Companion.putFileEntry
import java.time.LocalDate
import java.time.Period

object OrderedScrambles {
    fun generateOrderedScrambles(globalTitle: String?, generationDate: LocalDate, versionTag: String, zipOut: ZipOutputStream, parameters: ZipParameters, wcifConfig: WCIFRequestBinding) {
        val wcifSchedule = wcifConfig.wcif.schedule

        if (wcifSchedule.venues.isEmpty()) {
            return
        }

        val activityDays = wcifSchedule.activitiesWithLocalStartTimes
            .map { it.value.dayOfYear }
            .distinct()

        // hasMultipleDays gets a variable assigned on the competition creation using the website's form.
        // Online schedule fit to it and the user should not be able to put events outside it, but we double check here.
        // The next assignment fix possible mistakes (eg. a competition is assigned with 1 day, but events are spread among 2 days).
        val hasMultipleDays = wcifSchedule.hasMultipleDays || activityDays.size > 1
        val hasMultipleVenues = wcifSchedule.hasMultipleVenues

        // We consider the competition start date as the earlier activity from the schedule.
        // This prevents miscalculation of dates for multiple timezones.
        val competitionStartActivity = wcifSchedule.earliestActivity

        for (venue in wcifSchedule.venues) {
            val venueName = venue.fileSafeName
            val hasMultipleRooms = venue.hasMultipleRooms

            val timezone = venue.dateTimeZone
            val competitionStartDate = competitionStartActivity.getLocalStartTime(timezone)

            for (room in venue.rooms) {
                val roomName = room.fileSafeName

                val activitiesPerDay = room.activities
                    .groupBy {
                        Period.between(
                            competitionStartDate.atLocalStartOfDay(),
                            it.getLocalStartTime(timezone).atLocalStartOfDay()
                        ).days
                    }

                for ((nthDay, activities) in activitiesPerDay) {
                    val scrambles = wcifConfig.activityScrambleRequests
                        .filterKeys { it in activities }

                    val activitiesHaveScrambles = scrambles.values.any { it.isNotEmpty() }

                    if (activitiesHaveScrambles) {
                        val filenameDay = nthDay + 1

                        val parts = listOfNotNull(
                            "Printing/Ordered Scrambles/",
                            "$venueName/".takeIf { hasMultipleVenues },
                            "Day $filenameDay/".takeIf { hasMultipleDays },
                            "Ordered Scrambles",
                            " - $venueName".takeIf { hasMultipleVenues },
                            " - Day $filenameDay".takeIf { hasMultipleDays },
                            " - $roomName".takeIf { hasMultipleRooms },
                            ".pdf"
                        )

                        if (hasMultipleVenues || hasMultipleDays || hasMultipleRooms) {
                            // In addition to different folders, we stamp venue, day and room in the PDF's name
                            // to prevent different files with the same name.
                            val pdfFileName = parts.joinToString("")

                            val sortedScrambles = scrambles.entries
                                .sortedBy { it.key.getLocalStartTime(timezone) }
                                .flatMap { it.value }

                            val sheet = ScrambleRequest.requestsToCompletePdf(globalTitle, generationDate, versionTag, sortedScrambles)
                            zipOut.putFileEntry(pdfFileName, sheet.render(), parameters)
                        }
                    }
                }
            }
        }

        // Generate all scrambles ordered
        val allScramblesOrdered = wcifSchedule.activitiesWithLocalStartTimes.entries
            .sortedBy { it.value }
            .flatMap { wcifConfig.activityScrambleRequests[it.key].orEmpty() }
            .distinct()

        val pdfFileName = "Printing/Ordered Scrambles/Ordered $globalTitle - All Scrambles.pdf"

        val sheet = ScrambleRequest.requestsToCompletePdf(globalTitle, generationDate, versionTag, allScramblesOrdered)
        zipOut.putFileEntry(pdfFileName, sheet.render(), parameters)
    }
}
