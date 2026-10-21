package com.example.comp2100hackthon;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 演示专用小题库。题目和标准解释放在同一处，避免 Manager 用题目文本
 * 查询解释时出现大小写、标点不一致而查不到的情况。
 *
 * 将来可替换为用户创建的 Quiz 或数据库题库。
 */
public final class DemoQuizCatalog {

	private DemoQuizCatalog() {
		// 工具类不需要创建实例
	}

	public record Question(String text, String[] options, int correctIndex, String explanation) {
		// 防止界面代码修改题库本身的选项数组
		@Override
		public String[] options() {
			return options.clone();
		}
	}

	private static final Question[] QUESTIONS = {
			new Question(
					"What does DAO stand for?",
					new String[]{"Data Analysis Object", "Data Access Object", "Direct Application Output", "Digital Access Operation"},
					1,
					"DAO stands for Data Access Object. It keeps data access separate from business logic."
			),
			new Question(
					"What does the Facade design pattern provide?",
					new String[]{"A different database", "A sorting algorithm", "A simple interface to a complex subsystem", "A unique user ID"},
					2,
					"Facade provides a simpler interface to a group of classes, so client code does not need to know their internal details."
			),
			new Question(
					"Why do we use the Strategy design pattern?",
					new String[]{"To store passwords", "To avoid every interface", "To switch between interchangeable behaviours", "To copy all objects"},
					2,
					"Strategy defines a common interface for different algorithms or behaviours. The client can switch the strategy without changing its code."
			)
	};

	public static Question questionAt(int index) {
		return QUESTIONS[Math.floorMod(index, QUESTIONS.length)];
	}

	public static int count() {
		return QUESTIONS.length;
	}

	/**
	 * DemoExplanationProvider 使用题目全文作为 Map 的 Key。
	 * 在这里统一产生 Map，确保题目和解释始终一致。
	 */
	public static Map<String, String> explanations() {
		Map<String, String> result = new LinkedHashMap<>();
		for (Question question : QUESTIONS) {
			result.put(question.text(), question.explanation());
		}
		return result;
	}
}
