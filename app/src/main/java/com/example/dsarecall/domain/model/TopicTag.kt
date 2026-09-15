package com.example.dsarecall.domain.model

enum class TopicTag(val displayName: String) {
    ARRAYS("Arrays & Hashing"),
    TWO_POINTERS("Two Pointers"),
    SLIDING_WINDOW("Sliding Window"),
    STACK("Stack & Monotonic Stack"),
    BINARY_SEARCH("Binary Search"),
    LINKED_LIST("Linked List"),
    TREES("Trees & BST"),
    TRIE("Trie"),
    HEAP("Heap / Priority Queue"),
    BACKTRACKING("Backtracking"),
    GRAPHS("Graphs & Topo Sort"),
    ADVANCED_GRAPHS("Dijkstra & Union Find"),
    DYNAMIC_PROGRAMMING("Dynamic Programming"),
    GREEDY("Greedy Algorithms"),
    INTERVALS("Intervals"),
    MATH_GEOMETRY("Math & Geometry"),
    BIT_MANIPULATION("Bit Manipulation");

    companion object {
        fun fromString(value: String): TopicTag {
            return values().firstOrNull { 
                it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
            } ?: ARRAYS
        }
    }
}
