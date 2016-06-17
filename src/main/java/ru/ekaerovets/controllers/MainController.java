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
import ru.ekaerovets.model.Char;
import ru.ekaerovets.model.MobileSyncData;
import ru.ekaerovets.model.Pinyin;
import ru.ekaerovets.service.DSLHolder;
import ru.ekaerovets.service.PinyinParser;
import ru.ekaerovets.service.ZiService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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

    @Autowired
    private DSLHolder dslHolder;

    private Gson gson = new GsonBuilder().create();

    /**
     * Sync data with mobile application
     */
    @RequestMapping(method = RequestMethod.POST, value = "/sync_pinyin")
    private void syncPinyin(@RequestBody String input, HttpServletResponse resp) throws IOException {
        MobileSyncData data = gson.fromJson(input, MobileSyncData.class);
        MobileSyncData res = ziService.syncMobile(data);
        String json = gson.toJson(res);
        resp.getWriter().write(json);
        resp.getWriter().close();
    }

    /**
     * Returns data for the web-application
     */
    @RequestMapping(method = RequestMethod.GET, value = "/data")
    private void getData(HttpServletResponse resp) throws IOException {
        MobileSyncData data = ziService.loadData();
        String json = gson.toJson(data);
        resp.getWriter().write(json);
        resp.getWriter().close();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upsert_pinyin")
    public ResponseEntity<Void> upsertPinyin(@RequestBody String input) {
        Pinyin pinyin = gson.fromJson(input, Pinyin.class);
        ziService.upsertPinyin(pinyin);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/upsert_char")
    public ResponseEntity<Void> upsertChar(@RequestBody String input) {
        Char ch = gson.fromJson(input, Char.class);
        ziService.upsertChar(ch);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/set_stage")
    public ResponseEntity<Void> setStage(@RequestBody String input) {
        Map<String, String> params = gson.fromJson(input, new TypeToken<Map<String, String>>() {
        }.getType());
        ziService.setStage(params.get("word"), Integer.parseInt(params.get("stage")), "true".equals(params.get("chars")));
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/lookup/{word}")
    public void lookup(@PathVariable String word, HttpServletResponse resp) throws IOException {
        resp.getWriter().write(gson.toJson(dslHolder.lookup(word)));
        resp.getWriter().flush();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/table")
    public void getTable(HttpServletResponse resp) throws IOException {
        String html = pinyinParser.getHtml();
        resp.getWriter().write(html);
        resp.getWriter().flush();
    }

}