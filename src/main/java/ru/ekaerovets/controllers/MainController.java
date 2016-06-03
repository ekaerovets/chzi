package ru.ekaerovets.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.ekaerovets.model.*;
import ru.ekaerovets.service.PinyinParser;
import ru.ekaerovets.service.ZiService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author karyakin dmitry
 *         date 30.10.15.
 */
@Controller
public class MainController {

    @Autowired
    private ZiService ziService;

    @Autowired
    private PinyinParser pinyinParser;

    private Gson gson = new GsonBuilder().create();

    @RequestMapping(method = RequestMethod.POST, value = "/sync")
    public void sync(@RequestBody String input, HttpServletResponse resp) throws IOException {
        SyncData data = gson.fromJson(input, SyncData.class);
        if (!data.getSecret().equals("xuehanyu")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        SyncData res = ziService.syncData(data);
        String json = gson.toJson(res);
        resp.getWriter().write(json);
        resp.getWriter().close();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/sync_pinyin")
    private void syncPinyin(@RequestBody String input, HttpServletResponse resp) throws IOException {
        Type type = new TypeToken<List<PinyinDroid>>() {
        }.getType();
        List<PinyinDroid> data = gson.fromJson(input, type);
        List<PinyinDroid> res = ziService.syncPinyins(data);
        String json = gson.toJson(res);
        resp.getWriter().write(json);
        resp.getWriter().close();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/add_chars")
    public ResponseEntity<Void> addChars(@RequestBody String input) {
        Type type = new TypeToken<List<Char>>() {
        }.getType();
        List<Char> data = gson.fromJson(input, type);
        ziService.storeChars(data, false);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/update_pinyin/{count}")
    public ResponseEntity<Void> updatePinyins(@PathVariable int count) {
        ziService.updatePinyins(count);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/add_words")
    public ResponseEntity<Void> addWords(@RequestBody String input) {
        Type type = new TypeToken<List<Word>>() {
        }.getType();
        List<Word> words = gson.fromJson(input, type);
        ziService.addWords(words);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/insert_pinyins")
    public ResponseEntity<Void> insertPinyins(@RequestBody String input) {
        Type type = new TypeToken<List<Pinyin>>() {
        }.getType();
        List<Pinyin> pinyins = gson.fromJson(input, type);
        ziService.insertPinyins(pinyins);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/update_char")
    public ResponseEntity<Void> insertChar(@RequestBody String input) {
        Char c = gson.fromJson(input, Char.class);
        ziService.updateChar(c);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/update_pinyin")
    public ResponseEntity<Void> insertPinyin(@RequestBody String input) {
        Pinyin p = gson.fromJson(input, Pinyin.class);
        ziService.updatePinyin(p);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/table")
    public void getTable(HttpServletResponse resp) throws IOException {
        String html = pinyinParser.getHtml();
        resp.getWriter().write(html);
        resp.getWriter().flush();
    }

}
