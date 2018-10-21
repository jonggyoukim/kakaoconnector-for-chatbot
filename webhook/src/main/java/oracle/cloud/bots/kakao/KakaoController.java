package oracle.cloud.bots.kakao;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import oracle.cloud.bots.kakao.model.KAKAO;
import oracle.cloud.bots.kakao.model.Keyboard;
import oracle.cloud.bots.kakao.model.Message;
import oracle.cloud.bots.kakao.model.MessageButton;
import oracle.cloud.bots.kakao.model.Photo;

/**
 * @author jonggyou.kim@oracle.com
 */
@RestController
public class KakaoController {
	private static int WAIT_TIME =4500;

	@Value("${oracle.bots.kakao.uri}")
	private String botUri;

	@Value("${oracle.bots.kakao.secret}")
	private String botSecret;

	@SuppressWarnings("rawtypes")
	private static HashMap<String, BlockingQueue> queueMap = new HashMap<String, BlockingQueue>();
	private static HashMap<String, String> userMap = new HashMap<String, String>();
	private static HashMap<String, KAKAO> responseMap = new HashMap<String, KAKAO>();

	private static ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
	private static SimpleDateFormat df = new SimpleDateFormat("ss.SSS");
	private static Mac mac;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@RequestMapping(value = "/kakao", method = RequestMethod.POST)
	@ResponseBody
	String fromWebhook(@RequestBody Map<String, Object> bodyMap) throws JsonProcessingException {
		logger.info("/kakao request=[" + bodyMap + "]");

		String userId = mapper.convertValue(bodyMap.get("userId"), String.class);
		String payload = mapper.writeValueAsString(bodyMap);

		if (!userMap.containsKey(userId)) {
			logger.info("/kakao +-----------------------------------------+");
			logger.info("/kakao | 사용자[" + userId + "]가 없습니다.            ");
			logger.info("/kakao +-----------------------------------------+");
		} else {
			String threadId = userMap.get(userId);
			@SuppressWarnings("unchecked")
			BlockingQueue<String> queue = queueMap.get(threadId);
			try {
				queue.offer(payload, 5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return "ok";
	}

	@RequestMapping(value = "/keyboard", method = RequestMethod.GET)
	@ResponseBody
	public String keyboard() {
		return "{ \"type\" : \"buttons\", \"buttons\" : [\"안녕하세요!!\"] }";
	}

	@RequestMapping(value = "/friend", method = RequestMethod.POST)
	@ResponseBody
	public String friend(@RequestBody Map<String, Object> body) {
		logger.info("/friend POST [" + body + "]");
		return "ok";
	}

	@RequestMapping(value = "/friend/{user_key}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable("user_key") String user_key) {
		logger.info("/friend/{user_key} DELETE [" + user_key + "]");
		return "ok";
	}

	@RequestMapping(value = "/chat_room/{user_key}", method = RequestMethod.DELETE)
	@ResponseBody
	public String doDelete(@PathVariable("user_key") String user_key) {
		logger.info("/chat_room{user_key} DELETE [" + user_key + "]");
		return "ok";
	}

	@RequestMapping(value = "/message", method = RequestMethod.POST)
	@ResponseBody
	public String message(@RequestHeader Map<String, Object> headers, @RequestBody Map<String, Object> bodyMap) {
		logger.info("\n\n\n/message -----------------------------------------");
		logger.info("/message botUri=[" + botUri + "]");
		logger.info("/message secret=[" + botSecret + "]");
		logger.info("/message message body=[" + bodyMap + "]");

		KAKAO kakao = null;
		String response = null;

		// userId, text를 뽑아낸다.
		String userId = mapper.convertValue(bodyMap.get("user_key"), String.class);
		String text = mapper.convertValue(bodyMap.get("content"), String.class);

		logger.info("/message userId=[" + userId + "]");
		logger.info("/message text=[" + text + "]");

		if (userMap.containsKey(userId)) {
			String msg = "(심각) 한번에 하나씩! '" + text + "' 에는 응답하지 않겠어요!! ";
			return "{ \"message\":{ \"text\" : \"" + msg + "\" } }";
		}

		try {
			// 카카오로 보낼 메시지를 만든다. - payload
			String payload = makePayload(bodyMap);

			// userId - threadId - queue 를 연결한다.
			String threadId = Thread.currentThread().getName();
			userMap.put(userId, threadId);
			logger.info("/message 사용자[" + userId + "]를 셋팅했습니다.");
			BlockingQueue<String> queue = getQueue(threadId);

			// 오라클 봇으로 연결
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> entity = new HttpEntity<String>(payload, getHeaders(payload));
			logger.info("/message entry[" + entity.toString() + "]");
			ResponseEntity<String> responseEntity = restTemplate.exchange(botUri, HttpMethod.POST, entity,
					String.class);
			logger.info("/message exchanged");

			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				logger.info("/message 200 return");
				try {
					do {
						// 응답이 올 때 까지 기다림 4.2초
						long t1 = System.currentTimeMillis();

						response = queue.poll(WAIT_TIME, TimeUnit.MILLISECONDS);

						long t2 = System.currentTimeMillis();
						logger.info("응답시간(타임아웃:5초) : " + df.format(t2 - t1));

						// 받은 응답 체크
						if (response == null) {
							kakao = new KAKAO("서버의 응답이 느려서 대답할 수가없어요!!");
							// response = "{ \"message\":{ \"text\" : \"서버의 응답이
							// 느려서 대답할
							// 수가없어요!!\" } }";
							logger.info("/message response = null");
						} else {
							kakao = convertBotToKakao(response);

							logger.info("/message response=[" + response + "]");
						}
						logger.info("/message kakao=[" + kakao + "]");
						logger.info("/message kakako.chunked=[" + kakao.isChunked() + "]");

						if (kakao.isChunked()) {
							if (responseMap.containsKey(userId)) {
								KAKAO _kakao = responseMap.get(userId);
								_kakao.merge(kakao);
								kakao = _kakao;

								responseMap.put(userId, kakao);
							} else {
								responseMap.put(userId, kakao);
							}
							logger.info("/message 다음메시지가 있어 [" + userId + "]를 삭제 하지 않겠습니다.");
						} else {
							if (responseMap.containsKey(userId)) {
								KAKAO _kakao = responseMap.remove(userId);
								_kakao.merge(kakao);
								kakao = _kakao;
								logger.info("/message merged kakao=[" + kakao + "]");

								userMap.remove(userId);
							} else {
								userMap.remove(userId);
							}
							logger.info("/kakao [" + userId + "]를 삭제하였습니다.");
						}
						logger.info("/message responseHash.size = " + responseMap.size());
						logger.info("/message replyHash.size = " + userMap.size());
					} while (kakao.isChunked());
				} catch (InterruptedException e) {
					e.printStackTrace();
					userMap.remove(userId);
				}
			} // bot으로 request 성공일 때

		} catch (Exception e) {
			e.printStackTrace();
			kakao = new KAKAO(e.getMessage());
			userMap.remove(userId);
		}

		String result = null;
		try {
			result = mapper.writeValueAsString(kakao);
			logger.info("/message return=[" + result + "]");
		} catch (JsonProcessingException e) {
			result = "{ \"message\":{ \"text\" : " + e.getMessage() + " } }";
			logger.info(result);
			userMap.remove(userId);
		}
		return result;
	}

	KAKAO convertBotToKakao(String response) throws JsonProcessingException {
		if (response == null) {
			return null;
		}

		Map botMap = null;
		KAKAO kakao = new KAKAO();
		try {
			botMap = mapper.readValue(response, HashMap.class);

			String chuncked = (String) botMap.get("chunked");
			if (chuncked != null && chuncked.equalsIgnoreCase("true")) {
				kakao.setChunked(true);
			} else {
				kakao.setChunked(false);
			}
		} catch (IOException e) {
			throw new RuntimeException("Bot응답 json에서 Kakao용 json으로의 변환이 실패하였습니다.", e);
		}

		Message message = new Message();
		message.setText(mapper.convertValue(botMap.get("text"), String.class));

		Keyboard keyboard = null;
		if (botMap.get("choices") != null) {
			keyboard = new Keyboard();
			keyboard.setType("buttons");
			keyboard.setButtons(mapper.convertValue(botMap.get("choices"), List.class));
		}

		Photo photo = null;
		if (botMap.get("photo") != null) {
			photo = new Photo();
			photo.setUrl(mapper.convertValue(botMap.get("photo"), String.class));
			photo.setWidth(720);
			photo.setHeight(630);
			message.setPhoto(photo);
		}

		MessageButton messageButton = null;
		if (botMap.get("message_button") != null) {
			messageButton = new MessageButton();
			messageButton.setUrl(mapper.convertValue(botMap.get("message_button"), String.class));
			message.setMessage_button(messageButton);
		}

		kakao.setMessage(message);
		kakao.setKeyboard(keyboard);

		return kakao;
	}

	private HttpHeaders getHeaders(String payload) {
		HttpHeaders headers = new HttpHeaders();
		try {
			headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
			headers.add("x-hub-signature", makeSignature(botSecret, payload));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return headers;
	}

	private String makeSignature(String secret, String payload)
			throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

		if (mac == null) {
			mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		}
		char[] hash = Hex.encodeHex(this.mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
		StringBuilder builder = new StringBuilder("sha256=");
		builder.append(hash);
		String signature = builder.toString();

		return signature;
	}

	private String makePayload(Map<String, Object> bodyMap) throws JsonProcessingException {
		Map<String, Object> payloadMap = new HashMap<String, Object>();
		payloadMap.put("userId", bodyMap.get("user_key"));
		payloadMap.put("text", bodyMap.get("content"));
		String payload = mapper.writeValueAsString(payloadMap);
		logger.info("/message payload=[" + payload + "]");
		return payload;

	}

	private BlockingQueue<String> getQueue(String threadId) {
		BlockingQueue<String> queue = null;

		logger.info("/message threadid = " + threadId);
		if (!queueMap.containsKey(threadId)) {
			BlockingQueue<String> _queue = new ArrayBlockingQueue<>(1);
			queueMap.put(threadId, _queue);
			queue = _queue;
		} else {
			queue = queueMap.get(threadId);
		}
		queue.clear();
		return queue;
	}

}