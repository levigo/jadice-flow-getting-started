package org.jadice.tutorial;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import com.jadice.flow.controller.client.connector.FlowJobClient;
import com.jadice.flow.controller.client.connector.TokenProvider;
import com.jadice.flow.controller.client.impl.EurekaStorageService;
import com.jadice.flow.controller.model.Item;
import com.jadice.flow.controller.model.Part;
import com.jadice.flow.controller.rest.JobInformation;
import com.jadice.flow.controller.rest.JobRequest;

/**
 * A simple OCR test which performs the following actions:
 * <ul>
 * <li>Create an image</li>
 * <li>Use jadice flow OCR worker to retrieve the OCR result</li>
 * <li>Compare the result with the expected text</li>
 * </ul>
 */
class OCRTest {
  // Parameters to access this jadice flow bundle
  private static final String JF_CONTROLLER_URL = "http://localhost:8080";
  private static final String EUREKA_USERNAME = "user";
  private static final String EUREKA_PASSWORD = "password";
  private static final String EUREKA_ENDPOINT = "http://localhost:8085";
  private static final String JOB_TEMPLATE_NAME = "ocr";

  // create one client for the storage and one for the controller
  private static final EurekaStorageService storageService = new EurekaStorageService(EUREKA_ENDPOINT, EUREKA_USERNAME, EUREKA_PASSWORD);
  private static final FlowJobClient flowJobClient = new FlowJobClient(JF_CONTROLLER_URL,
      Duration.ofSeconds(60), TokenProvider.NOOP_PROVIDER);

  /**
   * A simple OCR test which performs the following actions:
   * <ul>
   * <li>Create an image containing text</li>
   * <li>Use jadice flow OCR worker to retrieve the OCR result</li>
   * <li>Compare the result with the expected text</li>
   * </ul>
   * 
   * @throws Exception if something goes wrong
   */
  @Test
  void test_correctOCRResult() throws Exception {
    // The text to recognize
    String text = "OCR test";

    // Create an image (or load via ImageIO.read(...) and adjust expected text)
    BufferedImage image = createImage();

    // Get byte contents (png)
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    byte[] imgData = baos.toByteArray();

    // Upload file with StorageService
    String fileName = "temp_ocr.png";
    String mimeType = "image/png";
    String uploadFile = storageService.upload(fileName, mimeType, new ByteArrayInputStream(imgData));
    System.out.println("uploaded file to " + uploadFile);

    // Call jadice flow
    String ocrResultPlainText = invokeJadiceFlowOCRWorker(uploadFile, mimeType);
    System.out.println("OCR result text: " + ocrResultPlainText);

    // Assert the result
    assertEquals(text, ocrResultPlainText);
  }

  /**
   * Creates the configuration parameters and send the OCR request to the jadice flow controller.
   * <p>
   * 
   * The timeouts in this Demo implementation are hard coded to 60sec.
   * 
   * @param uploadFile the URI for the uploaded image data
   * @param contentType mime type of image
   * @return the plain text OCR result
   * @throws Exception if something goes wrong
   */
  private String invokeJadiceFlowOCRWorker(String uploadFile, String contentType) throws Exception {
    // Invoke Job with FlowJobClient
    JobRequest jobRequest = prepareJobRequest(uploadFile, contentType);
    JobInformation job = flowJobClient.createJob(jobRequest);
    long jobExecutionID = job.getJobExecutionID();

    // Wait for completion
    JobInformation jobInformation = flowJobClient.awaitFinalJobState(jobExecutionID, 60000, TimeUnit.MILLISECONDS);
    String status = jobInformation.getStatus();

    Part[] resultParts = flowJobClient.getResultParts(jobExecutionID);
    Part resultPart = resultParts[0];
    if (resultPart != null) {
      String resultUri = resultPart.getUrl();

      // download result with StorageService
      System.out.println("downloading file from " + resultUri + " ...");
      InputStream inputStream = storageService.download(resultUri);

      return getResultText(inputStream);
    } else {
      throw new Exception(
          String.format("No result for job with id=%d - finished with status %s", job.getJobExecutionID(), status));
    }
  }

  private static JobRequest prepareJobRequest(String uploadUrl, String mimeType) {
    Item item = new Item();
    Part p = new Part();
    p.setMimeType(mimeType);
    p.setUrl(uploadUrl);
    item.addParts(Collections.singletonList(p));
    JobRequest request = new JobRequest();
    request.setJobTemplateName(JOB_TEMPLATE_NAME);
    request.getItems().add(item);
    return request;
  }

  /**
   * Utility method to get the result as string.
   * 
   * @return the result as string
   * @throws IOException if something goes wrong
   */
  private String getResultText(InputStream inputStream) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (sb.length() > 0) {
          sb.append(System.lineSeparator());
        }
        sb.append(line.trim());
      }
    }
    return sb.toString().trim();
  }

  /**
   * Creates a BufferedImage containing the text "OCR test".
   *
   * @return the BufferedImage
   */
  private static BufferedImage createImage() {
    String text = "OCR test";
    int width = 400;
    int height = 150;
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = (Graphics2D) img.getGraphics();

    g.setColor(Color.white);
    g.fillRect(0, 0, width, height);

    g.setColor(Color.black);
    g.setFont(new Font("dialog", Font.PLAIN, 24));
    g.drawString(text, 10, height / 2);

    return img;
  }
}