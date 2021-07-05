package com.buildit.assignment;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.buildit.assignment.exception.InvalidInputException;

public class SampleWebCrawler {

	//OWASP regex format to validate website format
	public static final String URL_REGEX = "^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))"
			+ "(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)" + "([).!';/?:,][[:blank:]])?$";

	private int MAX_DEPTH = 2;

	private HashSet<String> links;

	public SampleWebCrawler() {
		links = new HashSet<>();
	}

	public void getPageLinks(String seed, int maxDepth) throws InvalidInputException {

		if (!validateURL(seed))
			throw new InvalidInputException("URL provided is not in valid format");

		extractLinks(seed, maxDepth);
		System.out.println("Extracted the avaliable list of urls successfully!");
	}

	private void extractLinks(String url, int depth) {

		if ((!links.contains(url) && (depth < MAX_DEPTH))) {

			try {

				if (!url.contains("facebook") || !url.contains("twitter") || !url.contains("linkedin")) {
					links.add(url);
					System.out.println(">> Depth: " + depth + " [" + url + "]");
				}
				Document document = Jsoup.connect(url).get();
				Elements linksOnPage = document.select("a[href]");

				depth++;

				for (Element page : linksOnPage) {

					extractLinks(page.attr("abs:href"), depth);

				}

			} catch (IOException e) {
				System.err.println("For '" + url + "': " + e.getMessage());
			}
		}
	}

	private boolean validateURL(String url) {

		Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

		Matcher matcher = URL_PATTERN.matcher(url);

		return matcher.matches();
	}

	public static void main(String[] args) {
		try {
			
			if (args == null || args.length == 0 || args.length > 1) {
				System.out.println("seed URL should be provided as input");
				System.exit(0);
			}
			
			new SampleWebCrawler().getPageLinks(args[0], 0);
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

	}

}
