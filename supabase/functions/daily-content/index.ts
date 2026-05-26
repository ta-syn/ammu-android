import { createClient } from "https://esm.sh/@supabase/supabase-js@2.7.1";

Deno.serve(async (_req) => {
  try {
    console.log("Generating daily content...");
    
    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? '';
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '';
    const openRouterApiKey = Deno.env.get('OPENROUTER_API_KEY') ?? '';
    
    const supabase = createClient(supabaseUrl, supabaseKey);
    
    let generatedReminder = "আজকের দিনটি শুরু করুন ইতিবাচক মনোভাব নিয়ে এবং পরিবারের সবাইকে হাসিমুখে সম্ভাষণ জানান।";
    
    if (openRouterApiKey) {
      try {
        const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${openRouterApiKey}`,
            "HTTP-Referer": "https://aistudio.google.com",
            "X-Title": "Ammu App"
          },
          body: JSON.stringify({
            model: "openrouter/free",
            messages: [
              { role: "system", content: "তুমি 'আম্মু অ্যাসিস্ট্যান্ট' — বাংলাদেশি মায়েদের জন্য একটি AI সহকারী। আজ সকালের জন্য একটি চমৎকার ছোট ইসলামিক বা স্বাস্থ্য টিপস/উপদেশ দাও বাংলায় (১-২ বাক্য)।" },
              { role: "user", content: "আজকের টিপস দিন।" }
            ]
          })
        });
        const data = await response.json();
        const content = data.choices?.[0]?.message?.content;
        if (content) {
          generatedReminder = content.trim();
        }
      } catch (e) {
        console.error("Failed to generate with OpenRouter:", e);
      }
    }
    
    // Store in daily_content table
    const { data, error } = await supabase
      .from('daily_content')
      .insert([
        { 
          content: generatedReminder,
          content_type: 'reminder',
          created_at: new Date().toISOString()
        }
      ])
      .select();

    return new Response(
      JSON.stringify({ 
        success: true, 
        message: "Daily content generated and stored successfully.",
        content: generatedReminder,
        data: data,
        error: error ? error.message : null
      }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (error) {
    console.error("Error in daily-content:", error);
    return new Response(
      JSON.stringify({ error: (error as any).message }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});
