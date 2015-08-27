package plugins.floghelper.ui;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import freenet.client.DefaultMIMETypes;
import freenet.client.HighLevelSimpleClient;
import freenet.client.async.PersistenceDisabledException;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.MultiValueTable;
import freenet.support.api.HTTPRequest;
import plugins.floghelper.FlogHelper;

/**
 * Serves static files.
 */
public class StaticToadlet extends FlogHelperToadlet {
    public static final String STATIC_PATH = "/static/";
    private static final String prefix = FlogHelperToadlet.BASE_URI + STATIC_PATH;

    private static final String static_dir = "floghelper-static";

    public StaticToadlet(HighLevelSimpleClient highLevelSimpleClient) {
        super(highLevelSimpleClient, STATIC_PATH);
    }

    @Override
    public void getPageGet(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException {
        final String path = uri.getPath();
        assert path.startsWith(prefix);
        final String filename = path.substring(prefix.length());
        final String overridePath = FlogHelper.getPR().getNode().getCfgDir().getAbsolutePath() +
                "/" + static_dir + "/" + filename;

        InputStream in;
        try {
            in = new FileInputStream(overridePath);
        } catch (FileNotFoundException e) {
            in = StaticToadlet.class.getResourceAsStream("static/" + filename);
            if (in == null) {
                writeTextReply(ctx, 404, "Not found", String.format("No such file %s", filename));
                return;
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());
        int length = 0;
        while (true) {
            int value = in.read();
            if (value == -1) {
                break;
            }
            length++;
            out.write(value);
        }

        in.close();

        ctx.sendReplyHeadersStatic(200, "OK", null, DefaultMIMETypes.guessMIMEType(path, false),
                length, new Date());
        ctx.writeData(out.toByteArray());

        out.close();
    }

    @Override
    public void getPagePost(PageNode pageNode, URI uri, HTTPRequest request, ToadletContext ctx) throws ToadletContextClosedException, IOException, PersistenceDisabledException {
        MultiValueTable<String, String> headers = new MultiValueTable<>();
        headers.put("Allow", "GET");

        writeTextReply(ctx, 405, "Method not allowed", headers,
                "POST to a static resource doesn't make sense.");
    }
}
