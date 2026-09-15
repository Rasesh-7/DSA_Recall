package com.example.dsarecall.data.seed

import com.example.dsarecall.data.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class DatasetVerificationTest {

    @Test
    fun testNeetCodeContainsExactly150Problems() {
        val neetcodeMemberships = PreloadedProblems.ALL_SHEET_MEMBERSHIPS.filter { it.sourceSheet == "NEETCODE_150" }
        assertEquals(150, neetcodeMemberships.size)
    }

    @Test
    fun testStriverDatasetContainsCompleteList() {
        val striverMemberships = PreloadedProblems.ALL_SHEET_MEMBERSHIPS.filter { it.sourceSheet == "STRIVER_A2Z" }
        assertEquals(471, striverMemberships.size)
    }

    @Test
    fun testCanonicalIdsAreUnique() {
        val allIds = PreloadedProblems.ALL_PRELOADED_PROBLEMS.map { it.id }
        assertEquals(PreloadedProblems.ALL_PRELOADED_PROBLEMS.size, allIds.size)
        assertEquals(allIds.size, allIds.distinct().size)
    }

    @Test
    fun testNoAccidentalDuplicateCanonicalProblems() {
        val normalizedTitles = PreloadedProblems.ALL_PRELOADED_PROBLEMS.map { 
            it.title.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "") 
        }
        assertEquals(normalizedTitles.size, normalizedTitles.distinct().size)
    }

    @Test
    fun testSheetOrderingIsPreservedAndContinuous() {
        val neetcodePositions = PreloadedProblems.ALL_SHEET_MEMBERSHIPS
            .filter { it.sourceSheet == "NEETCODE_150" }
            .map { it.position }
            .sorted()

        val expectedNeetcodePositions = (1..150).toList()
        assertEquals(expectedNeetcodePositions, neetcodePositions)

        val striverPositions = PreloadedProblems.ALL_SHEET_MEMBERSHIPS
            .filter { it.sourceSheet == "STRIVER_A2Z" }
            .map { it.position }
            .sorted()

        val expectedStriverPositions = (1..471).toList()
        assertEquals(expectedStriverPositions, striverPositions)
    }

    @Test
    fun testEveryProblemHasRequiredInAppContent() {
        PreloadedProblems.ALL_PRELOADED_PROBLEMS.forEach { entity ->
            val problem = entity.toDomain()
            assertTrue("Problem ID must not be blank", problem.id.isNotBlank())
            assertTrue("Title must not be blank for ${problem.id}", problem.title.isNotBlank())
            assertTrue("Description must not be blank for ${problem.id}", problem.description.isNotBlank())
            assertTrue("Examples must not be empty for ${problem.id}", problem.examples.isNotEmpty())
            assertTrue("Constraints must not be empty for ${problem.id}", problem.constraints.isNotEmpty())
            assertTrue("Topic tags must not be empty for ${problem.id}", problem.topicTags.isNotEmpty())
        }
    }

    @Test
    fun testZeroExternalUrlsInProblemModels() {
        val forbiddenKeywords = listOf("http://", "https://", "leetcode.com", "geeksforgeeks.org", "neetcode.io", "takeuforward.org")

        PreloadedProblems.ALL_PRELOADED_PROBLEMS.forEach { entity ->
            val allText = "${entity.id} ${entity.title} ${entity.difficulty} ${entity.topicTagsJoined} ${entity.description} ${entity.examplesJoined} ${entity.constraintsJoined}"
            forbiddenKeywords.forEach { keyword ->
                assertFalse(
                    "Forbidden URL keyword '$keyword' found in problem ${entity.id}",
                    allText.lowercase(Locale.ROOT).contains(keyword.lowercase(Locale.ROOT))
                )
            }
        }
    }

    @Test
    fun testOverlapCountIsAccurate() {
        val problemToSheets = mutableMapOf<String, MutableList<String>>()
        PreloadedProblems.ALL_SHEET_MEMBERSHIPS.forEach { mem ->
            problemToSheets.getOrPut(mem.problemId) { mutableListOf() }.add(mem.sourceSheet)
        }
        val overlaps = problemToSheets.values.count { it.size > 1 }
        assertEquals(51, overlaps)
    }
}
