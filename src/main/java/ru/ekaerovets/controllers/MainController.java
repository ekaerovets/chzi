package ru.ekaerovets.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.ekaerovets.model.ItemWrapper;
import ru.ekaerovets.model.SyncData;
import ru.ekaerovets.service.BinaryService;
import ru.ekaerovets.service.DSLHolder;
import ru.ekaerovets.service.PinyinParser;
import ru.ekaerovets.service.ZiService;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    @ModelAttribute
    public void allowCrossOrigin(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    /**
     * Sync data with mobile application
     */
    @RequestMapping(method = RequestMethod.POST, value = "/mobile/sync")
    private void syncPinyin(@RequestBody byte[] input, HttpServletResponse resp) throws IOException {
        SyncData syncData = BinaryService.readSyncData(new DataInputStream(new ByteArrayInputStream(input)));
        ziService.backup(gson.toJson(syncData), true);
        SyncData res = ziService.syncMobile(syncData);
        BinaryService.writeSyncData(res, new DataOutputStream(resp.getOutputStream()));
        resp.getOutputStream().flush();
        resp.getOutputStream().close();
    }

    /**
     * Returns data for the web-application
     */
    @RequestMapping(method = RequestMethod.GET, value = "/data")
    private void getData(HttpServletResponse resp) throws IOException {
        SyncData data = ziService.loadData();
        String json = gson.toJson(data);
        resp.getWriter().write(json);
        resp.getWriter().close();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/insert")
    public ResponseEntity<Void> upsertChar(@RequestBody String input) {
        ItemWrapper wrapper = gson.fromJson(input, ItemWrapper.class);
        ziService.insertItem(wrapper);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/update")
    public ResponseEntity<Void> upsertWord(@RequestBody String input) {
        ItemWrapper wrapper = gson.fromJson(input, ItemWrapper.class);
        ziService.updateItem(wrapper);
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

    @RequestMapping(method = RequestMethod.POST, value = "/backup")
    public ResponseEntity<Void> backup(HttpServletResponse resp) {
        ziService.backup(gson.toJson(ziService.loadData()), false);
        return ResponseEntity.ok().build();
    }

}
