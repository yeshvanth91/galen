/*******************************************************************************
* Copyright 2015 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.galen.tests.speclang2;


import static net.mindengine.galen.components.TestUtils.deleteSystemProperty;
import static net.mindengine.galen.specs.Side.BOTTOM;
import static net.mindengine.galen.specs.Side.LEFT;
import static net.mindengine.galen.specs.Side.RIGHT;
import static net.mindengine.galen.specs.Side.TOP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import net.mindengine.galen.config.GalenConfig;
import net.mindengine.galen.parser.SyntaxException;
import net.mindengine.galen.speclang2.reader.specs.SpecReaderV2;
import net.mindengine.galen.specs.*;

import org.junit.BeforeClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SpecsReaderV2Test {

    private static final Properties EMPTY_PROPERTIES = new Properties();

    @BeforeClass
    public void init() throws IOException {
        deleteSystemProperty("galen.range.approximation");
        deleteSystemProperty("galen.reporting.listeners");
        GalenConfig.getConfig().reset();
    }

    @BeforeMethod
    public void configureApproximation() {
        System.setProperty("galen.range.approximation", "2");
    }

    @AfterMethod
    public void clearApproximation() {
        System.getProperties().remove("galen.range.approximation");
    }

    @Test
    public void shouldReadSpec_inside() throws IOException {
        Spec spec = readSpec("inside object");
        SpecInside specInside = (SpecInside) spec;

        assertThat(specInside.getObject(), is("object"));
        assertThat(specInside.getPartly(), is(false));

        List<Location> locations = specInside.getLocations();
        assertThat(locations.size(), is(0));
    }

    @Test
    public void shouldReadSpec_inside_object_10px_right() throws IOException {
        Spec spec = readSpec("inside object 10px right");
        SpecInside specInside = (SpecInside) spec;

        assertThat(specInside.getObject(), is("object"));
        assertThat(specInside.getPartly(), is(false));

        List<Location> locations = specInside.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(specInside.getLocations(), contains(new Location(Range.exact(10), sides(RIGHT))));
        assertThat(spec.getOriginalText(), is("inside object 10px right"));
    }

    @Test
    public void shouldReadSpec_inside_partly_object_10px_right()  throws IOException {
        Spec spec = readSpec("inside partly object 10px right");
        SpecInside specInside = (SpecInside) spec;

        assertThat(specInside.getObject(), is("object"));
        assertThat(specInside.getPartly(), is(true));

        List<Location> locations = specInside.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(specInside.getLocations(), contains(new Location(Range.exact(10), sides(RIGHT))));
        assertThat(spec.getOriginalText(), is("inside partly object 10px right"));
    }


    @Test
    public void shouldReadSpec_inside_object_10_to_30px_left()  throws IOException {
        Spec spec = readSpec("inside object 10 to 30px left");
        SpecInside specInside = (SpecInside) spec;

        assertThat(specInside.getObject(), is("object"));

        List<Location> locations = specInside.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(specInside.getLocations(), contains(new Location(Range.between(10, 30), sides(LEFT))));
        assertThat(spec.getOriginalText(), is("inside object 10 to 30px left"));
    }

    @Test
    public void shouldReadSpec_inside_object_25px_top_left()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside object 25px top left");

        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(25),sides(TOP, LEFT))));
        assertThat(spec.getOriginalText(), is("inside object 25px top left"));
    }

    @Test
    public void shouldReadSpec_inside_object_25px_top_left_comma_10_to_20px_bottom()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside object 25px top left, 10 to 20px bottom");

        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(2));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(25),sides(TOP, LEFT)),
                new Location(Range.between(10, 20), sides(BOTTOM))));
        assertThat(spec.getOriginalText(), is("inside object 25px top left, 10 to 20px bottom"));
    }

    @Test
    public void shouldReadSpec_inside_object_25px_bottom_right()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside object 25px bottom right");

        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(25),sides(BOTTOM, RIGHT))));
        assertThat(spec.getOriginalText(), is("inside object 25px bottom right"));
    }

    @Test
    public void shouldReadSpec_inside_object_25px_top_left_right_bottom()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside object 25px top left right bottom ");

        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(1));
        assertThat(spec.getLocations(), contains(new Location(Range.exact(25), sides(TOP, LEFT, RIGHT, BOTTOM))));
        assertThat(spec.getOriginalText(), is("inside object 25px top left right bottom"));
    }

    @Test public void shouldReadSpec_inside_object_20px_left_and_approximate_30px_top()  throws IOException {
        SpecInside spec = (SpecInside)readSpec("inside object 20px left, ~30px top");

        List<Location> locations = spec.getLocations();
        assertThat(locations.size(), is(2));

        Assert.assertEquals(new Location(Range.exact(20), sides(LEFT)), spec.getLocations().get(0));
        Assert.assertEquals(new Location(Range.between(28, 32), sides(TOP)), spec.getLocations().get(1));

        assertThat(spec.getOriginalText(), is("inside object 20px left, ~30px top"));
    }

    @Test(expectedExceptions = SyntaxException.class,
            expectedExceptionsMessageRegExp = "Missing object name"
    )
    public void shouldGiveError_inside_withoutObjects() throws IOException {
        readSpec("inside");
    }

    @Test(expectedExceptions = SyntaxException.class,
            expectedExceptionsMessageRegExp = "Missing object name"
    )
    public void shouldGiveError_inside_partly_withoutObjects() throws IOException {
        readSpec("inside partly");
    }

    @Test
    public void shouldReadSpec_contains()  throws IOException {
        Spec spec = readSpec("contains object, menu, button");
        SpecContains specContains = (SpecContains) spec;
        assertThat(specContains.getChildObjects(), contains("object", "menu", "button"));
        assertThat(spec.getOriginalText(), is("contains object, menu, button"));
    }

    @Test
    public void shouldReadSpec_contains_with_regex()  throws IOException {
        Spec spec = readSpec("contains menu-item-*");
        SpecContains specContains = (SpecContains) spec;
        assertThat(specContains.getChildObjects(), contains("menu-item-*"));
        assertThat(spec.getOriginalText(), is("contains menu-item-*"));
    }

    @Test
    public void shouldReadSpec_contains_partly()  throws IOException {
        Spec spec = readSpec("contains partly object, menu, button");
        SpecContains specContains = (SpecContains) spec;
        assertThat(specContains.isPartly(), is(true));
        assertThat(specContains.getChildObjects(), contains("object", "menu", "button"));
        assertThat(spec.getOriginalText(), is("contains partly object, menu, button"));
    }

    @Test(expectedExceptions = SyntaxException.class,
            expectedExceptionsMessageRegExp = "Missing object name"
    )
    public void shouldGiveError_contains_withoutObjects() throws IOException {
        readSpec("contains");
    }

    @Test(expectedExceptions = SyntaxException.class,
            expectedExceptionsMessageRegExp = "Missing object name"
    )
    public void shouldGiveError_contains_partly_withoutObjects() throws IOException {
        readSpec("contains partly");
    }

    private Spec readSpec(String specText) throws IOException {
        return new SpecReaderV2(EMPTY_PROPERTIES).read(specText);
    }

    private List<Side> sides(Side...sides) {
        return Arrays.asList(sides);
    }
}
