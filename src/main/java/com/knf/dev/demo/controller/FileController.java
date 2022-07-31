package com.knf.dev.demo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileController {

	// Save the uploaded file to this folder
	private static String UPLOADED_FOLDER =
			"/home/user/Desktop/files/";

	@GetMapping("/")
	public String index(Model model) {

		List<String> list = new ArrayList<String>();
		File files = new File(UPLOADED_FOLDER);
		String[] fileList = ((File) files).list();
		for (String name : fileList) {
			list.add(name);
		}
		model.addAttribute("list", list);
		return "upload";
	}

	@PostMapping("/upload")
	public String singleFileUpload
	(@RequestParam("file") MultipartFile file, Model model) {

		if (file.isEmpty()) {
			model.addAttribute("warning",
					"Please select a file to upload");
			return "upload";
		}

		try {

			byte[] bytes = file.getBytes();
			Path path = Paths.get(UPLOADED_FOLDER +
					file.getOriginalFilename());
			Files.write(path, bytes);

			model.addAttribute("message",
					"You successfully uploaded '"
			+ file.getOriginalFilename() + "'");

		} catch (IOException e) {
			model.addAttribute("error", "Error");
			return "upload";
		}

		List<String> list = new ArrayList<String>();
		File files = new File(UPLOADED_FOLDER);
		String[] fileList = ((File) files).list();
		for (String name : fileList) {
			list.add(name);
		}
		model.addAttribute("list", list);
		return "upload";
	}

	@GetMapping(path = "/download/{name}")
	public ResponseEntity<Resource> download
	  (@PathVariable("name") String name) throws IOException {

		File file = new File(UPLOADED_FOLDER + name);
		Path path = Paths.get(file.getAbsolutePath());
		ByteArrayResource resource = new ByteArrayResource
				(Files.readAllBytes(path));

		return ResponseEntity.ok().headers(this.headers(name))
				.contentLength(file.length())
				.contentType(MediaType.parseMediaType
						("application/octet-stream")).body(resource);
	}

	@PostMapping(path = "/delete")
	public String delete(@RequestParam("name") String name)
			throws IOException {

		try {
			Files.deleteIfExists(Paths.get(UPLOADED_FOLDER + name));
		}

		catch (IOException e) {
			return "redirect:/";
		}
		return "redirect:/";
	}

	private HttpHeaders headers(String name) {

		HttpHeaders header = new HttpHeaders();
		header.add(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=" + name);
		header.add("Cache-Control", "no-cache, no-store,"
				+ " must-revalidate");
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");
		return header;

	}
}
